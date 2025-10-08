import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Client {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("You need to enter the <path> to the file, the <IP-address> and the server <port>");
			return;
		}

		try {
			Client client = new Client(args[0], args[1], args[2]);
			client.sendFile();
		}
		catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	private String filePath;
	private String fileName;
	private int fileNameLength;
	private long fileSize = 0;
	private String serverAddress;
	private int serverPort;
	private Socket connectionSocket;

	Client (String path, String address, String port) {
		filePath = path;
		Path pathOfPath = Paths.get(filePath);
		fileName = pathOfPath.getFileName().toString();
		fileNameLength = fileName.getBytes(StandardCharsets.UTF_8).length;
		if (fileNameLength > 4096) {
			System.out.println("The file name is too long");
			return;
		}
		try {
			fileSize = Files.size(Paths.get(filePath));
		}
		catch (IOException e) {
			System.out.println("Cannot take file <" + fileName + "> size: " + e.getMessage());
			return;
		}
		double fileSizeTB = fileSize / (1024.0 * 1024.0 * 1024.0 * 1024.0);
		if (fileSizeTB > 1) {
			System.out.println("The file size is too long");
			return;
		}

		try {
			int supposedPort = Integer.parseInt(port);
			if ((0 <= supposedPort) && (supposedPort <= 65535))
				serverPort = supposedPort;
			else
				throw new Exception("The port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive: " + supposedPort);
		}
		catch (NumberFormatException nfe) {
			System.out.println("Cannot convert to the integer: " + nfe.getMessage());
			return;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		serverAddress = address;
	}

	public void sendFile() throws Exception {
		try (Socket connectionSocket = new Socket(InetAddress.getByName(serverAddress), serverPort)) {
			DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());

			out.writeInt(fileNameLength);

			byte[] byteFileName = fileName.getBytes(StandardCharsets.UTF_8);
			out.write(byteFileName, 0, fileNameLength);
			System.out.println("File name was sent");

			out.writeLong(fileSize);

			try (FileInputStream fileIn = new FileInputStream(filePath)) {
				byte[] filePart = new byte[1024];
				int filePartRead;
				while ((filePartRead = fileIn.read(filePart)) != -1)
					out.write(filePart, 0, filePartRead);
				out.flush();
			}
			System.out.println("File was sent. File size is " + fileSize);

			connectionSocket.shutdownOutput();

			boolean transferSuccess = in.readBoolean();
			if (transferSuccess)
				System.out.println("The file transfer was successful");
			else
				System.out.println("The file transfer was failed");
		}
		catch (UnknownHostException uhe) {
			throw new Exception("Cannot determines the IP address of a host: " + serverAddress);
		}
		catch (IOException ioe) {
			throw new Exception("Error: " + ioe.getMessage());
		}
	}
}
