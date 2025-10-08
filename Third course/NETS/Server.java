import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("You need to enter the server <port>");
			return;
		}

		try {
			Server server = new Server(args[0]);
			server.execute();
		}
		catch (IOException e) {
			System.out.println("Error: " +e.getMessage());
		}
	}

	private int serverPort = 5252;
	private ServerSocket serverSocket;
	private ConcurrentHashMap<Socket, ClientInfo> clients;
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private AtomicInteger clientsCounter = new AtomicInteger(0);

	Server (String port) throws IOException {
		try {
			int supposedPort = Integer.parseInt(port);
			if ((0 <= supposedPort) && (supposedPort <= 65535))
				serverPort = supposedPort;
			else
				throw new Exception("The port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive: " + supposedPort);
		}
		catch (NumberFormatException nfe) {
			System.out.println("Cannot convert to the integer: " + nfe.getMessage() + ". Will be set default port: 5252");
		}
		catch (Exception e) {
			System.out.println(e.getMessage() + ". Will be set default port: 5252");
		}

		clients = new ConcurrentHashMap<>();

		serverSocket = new ServerSocket(serverPort);
		System.out.println("Sever listening on the port: " + serverSocket.getLocalPort());
		scheduler.scheduleAtFixedRate(() -> showClientsInfo(), 1, 3, TimeUnit.SECONDS);
	}

	private void showClientsInfo() {
		Iterator<Map.Entry<Socket, ClientInfo>> it = clients.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Socket, ClientInfo> entry = it.next();
			showClientInfo(entry.getValue());
		}
	}

	private void showClientInfo(ClientInfo clientInfo) {
		String instanceSpeed = String.format("Instant Speed: %.2f KB/s", clientInfo.countInstantSpeed());
		String averageSpeed = String.format("Average Speed: %.2f KB/s", clientInfo.countAverageSpeed());

		System.out.println("Client number: " + clientInfo.getClientNumber());
		System.out.println(instanceSpeed);
		System.out.println(averageSpeed);
		System.out.println("");
	}

	public void execute() throws IOException {
		while (true) {
			Socket newClient = serverSocket.accept();
			clients.put(newClient, new ClientInfo(clientsCounter.incrementAndGet()));
			new Thread(() -> getFile(newClient)).start();
		}
	}

	private void getFile(Socket socket) {
		try (socket) {
			long startTime = System.currentTimeMillis();

			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			int fileNameLength = in.readInt();

			byte[] byteFileName = new byte[fileNameLength];
			in.readFully(byteFileName);
			String fileName = new String(byteFileName, StandardCharsets.UTF_8);

			long fileSize = in.readLong();

			try (FileOutputStream fileOut = makeFile(fileName)) {
				if (fileOut == null)
					throw new IOException("Cannot create file: " + fileName);

				byte[] filePart = new byte[1024];
				long wasRead = 0;
				int filePartRead;
				while ((filePartRead = in.read(filePart, 0, 1024)) != -1) {
					fileOut.write(filePart, 0, filePartRead);
					clients.get(socket).addReceived(filePartRead);
				}
			}

			long workingTime = System.currentTimeMillis() - startTime;
			if (TimeUnit.MILLISECONDS.toSeconds(workingTime) <= 3)
				showClientInfo(clients.get(socket));

			if (clients.get(socket).getTotalReceived() < fileSize)
				out.writeBoolean(false);
			else
				out.writeBoolean(true);

			out.flush();
		}
		catch (IOException e) {
			System.out.println("With client " + socket.getInetAddress().getHostAddress() + " this error: " + e.getMessage());
			return;
		}
		finally {
			clients.remove(socket);
		}
	}

	private FileOutputStream makeFile(String fileName) throws IOException {
		File dir = new File("uploads");
		if (!dir.exists())
			dir.mkdirs();

		String partName = fileName;
		String extension = "";
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex != -1) {
			partName = fileName.substring(0, dotIndex);
			extension = fileName.substring(dotIndex);
		}
		
		File file = new File(dir, fileName);
		int cnt = 1;
		while (file.exists())
			file = new File(dir, partName + "_" + cnt++ + extension);

		return new FileOutputStream(file);
	}
}