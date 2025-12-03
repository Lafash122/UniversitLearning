import java.io.*;
import java.nio.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import org.xbill.DNS.*;

public class SocksProxy {
	private enum SessionState {
		GREETING,
		REQUEST,
		CONNECTING,
		RELAYING,
		RESOLVING,
		CLOSED
	}

	private static class Session {
		SocketChannel client;
		SocketChannel remote;

		SelectionKey clientKey;
		SelectionKey remoteKey;

		ByteBuffer clientToRemoteBuff = ByteBuffer.allocateDirect(64 * 1024);
		ByteBuffer remoteToClientBuff = ByteBuffer.allocateDirect(64 * 1024);
		ByteBuffer messagesBuff = ByteBuffer.allocateDirect(2 * 1024);

		SessionState state = SessionState.GREETING;

		String targetHost;
		int targetPort;

		volatile boolean endRemoteChannel = false;
		volatile boolean endClientChannel = false;

		Session (SocketChannel client) {
			this.client = client;
		}

		public boolean isClosed() {
			return (state == SessionState.CLOSED);
		}

		public void close() {
			try {
				if (client != null)
					client.close();
			} catch (IOException io) {}
			try {
				if (remote != null)
					remote.close();
			} catch (IOException io) {}
			if (clientKey != null)
				clientKey.cancel();
			if (remoteKey != null)
				remoteKey.cancel();

			state = SessionState.CLOSED;
		}
	}

	private static class DnsQuerry {
		Session dnsSession;
		final long waitingTime;

		DnsQuerry(Session session) {
			dnsSession = session;
			waitingTime = System.nanoTime();
		}
	}

	private static final long DNS_TIMEOUT = 8_000_000_000L;
	private static final long SELECTOR_TIMEOUT = 1_000;
	private int port = 5252;
	private final Selector selector;
	private final ServerSocketChannel serverChannel;
	private final DatagramChannel dnsChannel;
	private final InetSocketAddress dnsResolver;

	private final Map<Integer, DnsQuerry> dnsQuerries = new ConcurrentHashMap<>();

	public SocksProxy(int suggestedPort) throws IOException {
		if ((suggestedPort < 0) || (suggestedPort > 65535))
			System.out.println("The number " + suggestedPort + " is not within the acceptable range of the port. Will be set default: 5252");
		else
			port = suggestedPort;

		selector = Selector.open();

		serverChannel = ServerSocketChannel.open();
		serverChannel.bind(new InetSocketAddress(port));
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		dnsChannel = DatagramChannel.open();
		dnsChannel.bind(null);
		dnsChannel.configureBlocking(false);
		dnsChannel.register(selector, SelectionKey.OP_READ);

		dnsResolver = ResolverConfig.getCurrentConfig().server();

		System.out.println("SOCKS5 proxy listening on port " + port);
	}

	public void execute() throws IOException {
		while (true) {
			long currentTime = System.nanoTime();
			List<Integer> toRemove = new ArrayList<>();
			for(Map.Entry<Integer, DnsQuerry> querry : dnsQuerries.entrySet())
				if ((currentTime - querry.getValue().waitingTime) > DNS_TIMEOUT) {
					Session s = querry.getValue().dnsSession;
					try {
						sendErrorToClient(s, (byte) 0x04);
					} catch (IOException io) {}
					s.close();
					toRemove.add(querry.getKey());
				}
			for (Integer id : toRemove)
				dnsQuerries.remove(id);

			int readyChannelsNumber = selector.select(SELECTOR_TIMEOUT);
			if (readyChannelsNumber == 0)
				continue;

			Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
			while(keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				keyIterator.remove();
				if (!key.isValid())
					continue;

				try {
					if (key.isAcceptable()) {
						handleAccept(key);
						continue;
					}

					if (key.isReadable())
						if (key.channel() == dnsChannel) {
							handleDnsRead();
							continue;
						}
						else
							handleRead(key);

					if (key.isValid() && key.isWritable())
						handleWrite(key);

					if (key.isValid() && key.isConnectable())
						handleRemoteConnect(key);
				}
				catch (IOException io) {
					Object att = key.attachment();
					if (att instanceof Session)
						((Session) att).close();

					System.out.println("Something gone wrong: " + io.getMessage());
				}
			}
		}
	}

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel socketChannel = serverChannel.accept();
		if (socketChannel == null)
			return;

		socketChannel.configureBlocking(false);
		Session session = new Session(socketChannel);
		SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ, session);
		session.clientKey = clientKey;
	}

	private void handleDnsRead() throws IOException {
		ByteBuffer dnsBuffer = ByteBuffer.allocate(4 * 1024);
		SocketAddress sender = dnsChannel.receive(dnsBuffer);
		if (sender == null)
			return;

		dnsBuffer.flip();
		try {
			Message msg = new Message(dnsBuffer);
			int dnsQuerryId = msg.getHeader().getID();
			DnsQuerry dnsQuerry = dnsQuerries.remove(dnsQuerryId);
			if (dnsQuerry == null)
				return;

			InetAddress resolved = null;
			List<org.xbill.DNS.Record> answers = msg.getSection(Section.ANSWER);
			for (org.xbill.DNS.Record answer : answers)
				if (answer instanceof ARecord) {
					resolved = ((ARecord) answer).getAddress();
					break;
				}

			Session session = dnsQuerry.dnsSession;
			if (resolved == null) {
				sendErrorToClient(session, (byte) 0x04);
				session.close();
			}
			else {
				startConnection(session, resolved);
			}
		} catch (IOException io) {}
	}

	private void resolveHostName(Session session) {
		try {
			Name resolvingName = Name.fromString(session.targetHost.endsWith(".") ? session.targetHost : session.targetHost + ".");
			org.xbill.DNS.Record r = org.xbill.DNS.Record.newRecord(resolvingName, Type.A, DClass.IN);
			Message msg = Message.newQuery(r);

			if (dnsQuerries.size() >= 65536)
				throw new IOException("DNS Queries map is already full");

			int querryId = ThreadLocalRandom.current().nextInt(1, 65536);
			while (dnsQuerries.containsKey(querryId))
				querryId = ThreadLocalRandom.current().nextInt(1, 65536);
			msg.getHeader().setID(querryId);

			byte[] data = msg.toWire();
			ByteBuffer toDns = ByteBuffer.wrap(data);
			dnsChannel.send(toDns, dnsResolver);
			dnsQuerries.put(querryId, new DnsQuerry(session));

			session.state = SessionState.RESOLVING;
		}
		catch (IOException e) {
			try {
				sendErrorToClient(session, (byte) 0x04);
			} catch (IOException io) {}
			session.close();
		}
	}

	private void handleRead(SelectionKey key) throws IOException {
		Session session = (Session) key.attachment();
		if ((session == null) || session.isClosed() || !key.isValid())
			return;

		if (key.channel() == session.client)
			handleClientRead(session);
		else if (key.channel() == session.remote)
			handleRemoteRead(session);
			
	}

	private void handleClientRead(Session session) throws IOException {
		if (session.isClosed())
			return;

		if ((session.state == SessionState.GREETING) || (session.state == SessionState.REQUEST)) {
			int readed = session.client.read(session.messagesBuff);
			if (readed == -1) {
				session.close();
				return;
			}
			if (readed == 0)
				return;

			session.messagesBuff.flip();
			if (session.state == SessionState.GREETING)
				if(!handleGreeting(session)) {
					session.messagesBuff.compact();
					return;
				}
					
			if (session.state == SessionState.REQUEST)
				if(!handleRequest(session)) {
					session.messagesBuff.compact();
					return;
				}

			session.messagesBuff.compact();
			return;
		}

		if (session.state == SessionState.RELAYING) {
			ByteBuffer dest = session.clientToRemoteBuff;
			int readed = session.client.read(dest);
			if (readed == -1) {
				try {
					if (session.remote != null)
						session.remote.shutdownOutput();
				} catch (IOException io) {}

				if ((session.clientKey != null) && session.clientKey.isValid() && !session.isClosed())
					session.clientKey.interestOps(session.clientKey.interestOps() & ~SelectionKey.OP_READ);

				session.endClientChannel = true;
				closeOnEnd(session);
			
				return;
			}
			if (readed == 0)
				return;

			if (dest.position() > 0)
				if ((session.remoteKey != null) && session.remoteKey.isValid() && !session.isClosed())
					session.remoteKey.interestOps(session.remoteKey.interestOps() | SelectionKey.OP_WRITE);

			if (dest.position() == dest.capacity())
				if (!dest.hasRemaining())
					session.clientKey.interestOps(session.clientKey.interestOps() & ~SelectionKey.OP_READ);
		}
	}

	private boolean handleGreeting(Session session) throws IOException {
		ByteBuffer buff = session.messagesBuff;
		if (buff.remaining() < 2)
			return false;

		buff.mark();
		byte version = buff.get();
		byte methodsNumber = buff.get();
		if (buff.remaining() < (methodsNumber & 0xFF)) {
			buff.reset();
			return false;
		}
		boolean isAuth = false;

		for (int i = 0; i < (methodsNumber & 0xFF); i++) {
			byte method = buff.get();
			if (method == 0x00)
				isAuth = true;
		}

		if (!isAuth) {
			sendAuthMethod(session, (byte) 0xFF);
			session.close();

			return false;
		}

		sendAuthMethod(session, (byte) 0x00);
		session.state = SessionState.REQUEST;

		return true;
	}

	private boolean handleRequest(Session session) throws IOException {
		ByteBuffer buff = session.messagesBuff;
		if (buff.remaining() < 10)
			return false;

		buff.mark();
		byte version = buff.get();
		byte code = buff.get();
		buff.get();
		byte type = buff.get();

		if ((version != 0x05) || (code != 0x01)) {
			sendErrorToClient(session, (byte) 0x07);
			session.close();

			return false;
		}
		if (type == 0x01) {
			if (buff.remaining() < 6) {
				buff.reset();
				return false;
			}

			byte[] ipAddress = new byte[4];
			buff.get(ipAddress);
			int toPort = (buff.get() & 0xFF) << 8 | (buff.get() & 0xFF);
			InetAddress address = InetAddress.getByAddress(ipAddress);
			session.targetHost = new String(ipAddress, "UTF-8");
			session.targetPort = toPort;

			startConnection(session, address);
		}
		else if (type == (byte) 0x03) {
			if (buff.remaining() < 1) {
				buff.reset();
				return false;
			}

			byte len = buff.get();
			if (buff.remaining() < (len + 2)) {
				buff.reset();
				return false;
			}

			byte[] byteName = new byte[len];
			buff.get(byteName);
			int toPort = (buff.get() & 0xFF) << 8 | (buff.get() & 0xFF);
			String hostName = new String(byteName, "UTF-8");
			session.targetHost = hostName;
			session.targetPort = toPort;

			resolveHostName(session);
		}
		else {
			sendErrorToClient(session, (byte) 0x08);
			session.close();

			return false;
		}

		return true;
	}

	private void startConnection(Session session, InetAddress address) throws IOException {
		if (session.isClosed())
			return;

		session.remote = SocketChannel.open();
		session.remote.configureBlocking(false);
		try {
			session.remote.connect(new InetSocketAddress(address, session.targetPort));
		}
		catch (UnresolvedAddressException e) {
			sendErrorToClient(session, (byte) 0x04);
			session.close();

			return;
		}

		session.remoteKey = session.remote.register(selector, SelectionKey.OP_CONNECT, session);
		session.state = SessionState.CONNECTING;
	}

	private void handleRemoteRead(Session session) throws IOException {
		if (session.isClosed())
			return;

		ByteBuffer dest = session.remoteToClientBuff;
		int readed = session.remote.read(dest);
		if (readed == -1) {
			try {
				if (session.client != null)
					session.client.shutdownOutput();
			} catch (IOException io) {}

			if ((session.remoteKey != null) && session.remoteKey.isValid() && !session.isClosed())
				session.remoteKey.interestOps(session.remoteKey.interestOps() & ~SelectionKey.OP_READ);

			session.endRemoteChannel = true;
			closeOnEnd(session);

			return;
		}
		if (readed == 0)
			return;

		if (dest.position() > 0)
			if ((session.clientKey != null) && session.clientKey.isValid() && !session.isClosed())
				session.clientKey.interestOps(session.clientKey.interestOps() | SelectionKey.OP_WRITE);

		if (dest.position() == dest.capacity())
			if (!dest.hasRemaining())
				session.remoteKey.interestOps(session.remoteKey.interestOps() & ~SelectionKey.OP_READ);

	}

	private void handleRemoteConnect(SelectionKey key) throws IOException {
		Session session = (Session) key.attachment();
		if ((session == null) || (session.remote == null) || !key.isValid() || session.isClosed())
			return;

		SocketChannel channel = (SocketChannel) key.channel();
		if (channel.finishConnect()) {
			InetSocketAddress localBind = (InetSocketAddress) channel.getLocalAddress();
			InetAddress bindedAddress = localBind.getAddress();
			int bindedPort = localBind.getPort();
			sendResponseToClient(session, bindedAddress, bindedPort);
			session.state = SessionState.RELAYING;

			if ((session.clientKey != null) && session.clientKey.isValid())
				session.clientKey.interestOps(session.clientKey.interestOps() | SelectionKey.OP_READ);
			if ((session.remoteKey != null) && session.remoteKey.isValid())
				session.remoteKey.interestOps(session.remoteKey.interestOps() | SelectionKey.OP_READ);
		}
	}

	private void handleWrite(SelectionKey key) throws IOException {
		Session session = (Session) key.attachment();
		if ((session == null) || session.isClosed() || !key.isValid() || session.isClosed())
			return;

		if (key.channel() == session.client)
			handleClientWrite(session);
		else if (key.channel() == session.remote)
			handleRemoteWrite(session);
	}

	private void handleClientWrite(Session session) throws IOException {
		ByteBuffer buff = session.remoteToClientBuff;
		buff.flip();
		if (buff.hasRemaining())
			session.client.write(buff);
		if (!buff.hasRemaining())
			if ((session.clientKey != null) && session.clientKey.isValid())
				session.clientKey.interestOps(session.clientKey.interestOps() & ~SelectionKey.OP_WRITE);

		buff.compact();
		if ((session.remoteKey != null) && session.remoteKey.isValid())
			session.remoteKey.interestOps(session.remoteKey.interestOps() | SelectionKey.OP_READ);

		closeOnEnd(session);
	}

	private void handleRemoteWrite(Session session) throws IOException {
		ByteBuffer buff = session.clientToRemoteBuff;
		buff.flip();
		if (buff.hasRemaining())
			session.remote.write(buff);
		if (!buff.hasRemaining())
			if ((session.remoteKey != null) && session.remoteKey.isValid())
				session.remoteKey.interestOps(session.remoteKey.interestOps() & ~SelectionKey.OP_WRITE);

		buff.compact();
		if ((session.clientKey != null) && session.clientKey.isValid())
			session.clientKey.interestOps(session.clientKey.interestOps() | SelectionKey.OP_READ);

		closeOnEnd(session);
	}

	private void closeOnEnd(Session session) throws IOException {
		boolean isBuffersEmpty = (session.clientToRemoteBuff.position() == 0) && (session.remoteToClientBuff.position() == 0);

		if (session.endClientChannel && session.endRemoteChannel && isBuffersEmpty)
			session.close();
	}

	private void sendAuthMethod(Session session, byte method) throws IOException {
		ByteBuffer out = ByteBuffer.wrap(new byte[] { (byte) 0x05, method });
		toClient(session, out);
	}

	private void sendErrorToClient(Session session, byte errorCode) throws IOException {
		ByteBuffer error = ByteBuffer.allocate(10);
		error.put((byte) 0x05);
		error.put(errorCode);
		error.put((byte) 0x00);
		error.put((byte) 0x01);
		error.put(new byte[] { 0, 0, 0, 0 });
		error.putShort((short) 0);
		error.flip();

		toClient(session, error);
	}

	private void sendResponseToClient(Session session, InetAddress address, int fromPort) throws IOException {
		ByteBuffer response = ByteBuffer.allocate(10);
		response.put((byte) 0x05);
		response.put((byte) 0x00);
		response.put((byte) 0x00);
		response.put((byte) 0x01);
		response.put(address.getAddress());
		response.putShort((short) fromPort);
		response.flip();

		toClient(session, response);
	}

	private void toClient(Session session, ByteBuffer data) throws IOException {
		session.client.write(data);
		if (data.hasRemaining()) {
			data.flip();
			session.remoteToClientBuff.put(data);
			if ((session.clientKey != null) && session.clientKey.isValid())
				session.clientKey.interestOps(session.clientKey.interestOps() | SelectionKey.OP_WRITE);
		}
	}
}