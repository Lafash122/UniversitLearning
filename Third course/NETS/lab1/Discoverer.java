import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Discoverer {
	private final int port = 5252;
	private final int sendingInterval = 250;
	private final int timeout = 1000;
	private final String sendingMessage = "MSGTOMCST";

	private InetAddress multicastAddress;
	private boolean isIPv4;
	private NetworkInterface multicastInterface;
	private MulticastSocket socket;

	private final ConcurrentMap<String, Integer> lastSeen = new ConcurrentHashMap<>();
	private final String copyId = UUID.randomUUID().toString();

	public Discoverer(String address) {
		try {
			setAddress(address);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		if (!multicastAddress.isMulticastAddress()) {
			System.out.println("This address: <" + multicastAddress.getHostAddress() + "> is not multicast address");
			return;
		}

		try {
			multicastInterface = findNetworkIface();
			if (multicastInterface == null) {
				System.out.println("Couldn't find the network interface");
				return;
			}
		}
		catch (Exception e) {
			System.out.println("Network error: " + e.getMessage());
			return;
		}

		try {
			socket = new MulticastSocket(null);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(port));
			socket.setNetworkInterface(multicastInterface);
			socket.joinGroup(new InetSocketAddress(multicastAddress, port), multicastInterface);
			socket.setTimeToLive(16);
		}
		catch (Exception e) {
			System.out.println("Socket error: " + e.getMessage());
			return;
		}

		System.out.println("My UUID is " + copyId + "\n");

		startExecution();
	}

	private void setAddress(String address) throws Exception {
		try {
			multicastAddress = InetAddress.getByName(address);
			if (multicastAddress instanceof Inet4Address) {
				System.out.println("IPv4 " + multicastAddress.getHostAddress());
				isIPv4 = true;
			}
			else if (multicastAddress instanceof Inet6Address) {
				System.out.println("IPv6 " + multicastAddress.getHostAddress());
				isIPv4 = false;
			}
		}
		catch (UnknownHostException e) {
			throw new Exception("Invalid Address Format: " + e.getMessage());
		}
		
	}

	private NetworkInterface findNetworkIface() throws Exception {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface res = ifaces.nextElement();
			if (res.isUp() && (!res.isLoopback()) && res.supportsMulticast())
				for (InterfaceAddress addresses : res.getInterfaceAddresses()) {
					InetAddress addr = addresses.getAddress();
					if (isIPv4 && (addr instanceof Inet4Address)) {
						System.out.println("This interface was found: " + addr.getHostAddress() + " " + res.getDisplayName());
						return res;
					}
					else if ((!isIPv4) && (addr instanceof Inet6Address)){
						System.out.println("This interface was found: " + addr.getHostAddress() + " " + res.getDisplayName());
						return res;
					}
				}
		}

		return null;
	}

	private void startExecution() {
		new Thread(() -> {
			while (!(socket.isClosed())) {
				try {
					Thread.sleep(sendingInterval);
				}
				catch (InterruptedException ie) {
					System.out.println(ie.getMessage());
				}
				sendMessage();
			}
		}).start();

		new Thread(() -> {
			byte[] buffer = new byte[1024];
			DatagramPacket pack = new DatagramPacket(buffer, buffer.length);

			while (!(socket.isClosed())) {
				try {
					pack.setLength(buffer.length);
					socket.receive(pack);
					String data = new String(pack.getData(), 0, pack.getLength());
					if (data.startsWith(sendingMessage)) {
						String senderId = data.substring(sendingMessage.length());
						Integer changed = lastSeen.put(senderId, (int)System.currentTimeMillis());

						if (changed == null)
							printAlives();
					}
				}
				catch (IOException io) {
					if (!socket.isClosed())
						System.out.println("Receiving error: " + io.getMessage());
				}
			}
		}).start();

		new Thread(() -> {
			while (!(socket.isClosed())) {
				try {
					Thread.sleep(timeout);
				}
				catch (InterruptedException ie) {
					System.out.println(ie.getMessage());
				}

				boolean isChanged = checkAlives();
				if (isChanged)
					printAlives();
			}
			
		}).start();
	}

	private void sendMessage() {
		String message = sendingMessage + copyId;
		byte[] buffer = message.getBytes();
		try {
			DatagramPacket pack = new DatagramPacket(buffer, buffer.length, multicastAddress, port);
			socket.send(pack);
		}
		catch (IOException io) {
			System.out.println("Sending error: " + io.getMessage());
		}
		catch (IllegalArgumentException ie) {
			System.out.println(ie.getMessage());
		}
	}

	private void printAlives() {
		StringBuilder str = new StringBuilder("\"Alived\" copies: \n");
		for (String id : lastSeen.keySet())
			str.append(id).append("\n");

		System.out.println(str.toString() + "\n");
	}

	private boolean checkAlives() {
		int curTime = (int) System.currentTimeMillis();
		boolean isChanged = false;
		Iterator<Map.Entry<String, Integer>> it = lastSeen.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> entry = it.next();
			if ((curTime - entry.getValue()) > timeout) {
				it.remove();
				isChanged = true;
			}
		}

		return isChanged;
	}
}

