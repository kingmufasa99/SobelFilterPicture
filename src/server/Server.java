package server;

import commun.Message;
import commun.Utils;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Server {

	private static Scanner reader = new Scanner(System.in);
	private static int nClients = 0;

	public static void main(String[] args) throws Exception {

		String serverIp = Utils.getValidAddressFromUser();
		int port = Utils.getValidPortFromUser();
		ServerSocket listener;
		InetAddress locIP = InetAddress.getByName(serverIp);
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		try {
			listener.bind(new InetSocketAddress(locIP, port));
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			System.out.println("Ending program");
			listener.close();
			reader.close();
			return;
		}

		// Server started first
		reader.close();
		System.out.format("The program server is running on %s:%d%n", serverIp, port);

		// Wait for clients to connect after
		try {
			while (true) {
				Socket socket = listener.accept();
				nClients++;
				new ServerThread(socket, nClients).start();
			}
		} finally {
			listener.close();
		}
	}

	private static class ServerThread extends Thread {
		private Socket socket;
		private int clientNumber;
		private String credentialsFilePath;

		public ServerThread(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			this.credentialsFilePath = "credentials.txt";
			System.out.println("New connection: client #" + this.clientNumber);
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
				DataInputStream dis = new DataInputStream(this.socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());

				String username = null;

				while (true) {
					username = Utils.readNextLineFromSocket(in);
					String password = Utils.readNextLineFromSocket(in);
					if (this.credentialsMatch(username, password)) {
						out.println(Message.LOGIN_SUCCESS);
						break;
					} else {
						out.println(Message.LOGIN_FAIL);
					}
				}

				while (true) {
					if (in.ready()) {
						String command = Utils.readNextLineFromSocket(in);
						if (command.equals("DISCONNECT")) {
							System.out.println("Client #" + this.clientNumber + " disconnected.");
							break;
						}
					}

					String fileName = Utils.readNextLineFromSocket(in);
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
					Date date = new Date();


					ByteArrayOutputStream dataContainer = new ByteArrayOutputStream();
					byte[] dataChunk = new byte[Utils.DATA_BUFFER_SIZE];
					int nBytesReceived = 0;
					do {
						nBytesReceived = dis.read(dataChunk);
						if (nBytesReceived < 0) {
							throw new IOException("Error reading data from client.");
						}
						dataContainer.write(dataChunk, 0, nBytesReceived);
					} while (nBytesReceived == Utils.DATA_BUFFER_SIZE);

					byte[] allData = dataContainer.toByteArray();
					ByteArrayInputStream byis = new ByteArrayInputStream(allData);
					BufferedImage originalImage = ImageIO.read(byis);
					BufferedImage sobelImage = Sobel.process(originalImage);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(sobelImage, "jpg", baos);
					baos.flush();
					byte[] imageBytes = baos.toByteArray();
					baos.close();
					dos.write(imageBytes, 0, imageBytes.length);

				}
			} catch (IOException e) {
				System.out.println("Error with client #" + this.clientNumber + " : " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Closing socket failed for client #" + this.clientNumber);
				}
			}
		}

		public boolean credentialsMatch(String username, String password) {
			File file = new File(this.credentialsFilePath);
			if (file.exists()) {
				// Check for username
				Scanner input = null;
				try {
					input = new Scanner(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				String line;
				while (input.hasNextLine()) {
					line = input.nextLine();
					if (line.compareTo(username) == 0) {
						line = input.nextLine();
						// Return true if password matches username, false if not
						return (line.compareTo(password) == 0);
					}
				}
				// New credentials
				return this.insertNewCredentials(username, password);
			} else {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
					return false;
				} finally {
					// Create first credentials
					return this.insertNewCredentials(username, password);
				}
			}
		}

		private boolean insertNewCredentials(String username, String password) {
			Writer output;
			try {
				output = new BufferedWriter(new FileWriter(this.credentialsFilePath, true));
				output.append(username + "\r\n");
				output.append(password + "\r\n");
				output.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
