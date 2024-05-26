package client;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.imageio.ImageIO;

import commun.Message;
import commun.Utils;

public class Client {

	public BufferedReader in;
	public PrintWriter out;
	private Socket socket;
	public DataInputStream dis;
	public DataOutputStream dos;
	public static int port;
	public static String serverAddress;
	public static String username;
	public static String newFilename;

	Client() {}

	public void connectToServer() throws IOException {
		// Get the server address from a dialog box.
		serverAddress = Utils.getValidAddressFromUser();
		port = Utils.getValidPortFromUser();
		socket = new Socket(serverAddress, port);

		System.out.format("Connected to %s:%d%n", serverAddress, port);
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
	}

	public void disconnect() throws IOException {
		out.println("DISCONNECT");
		socket.close();
		System.out.println("Disconnected from server");
	}

	public static void main(String[] args) throws Exception {
		Client client = new Client();
		try {
			client.connectToServer();
		} catch(Exception e) {
			System.out.println("Error while connecting -- " + e.getMessage());
			return;
		}

		// Perform login
		String response;
		do {
			username = Utils.enterUsername();
			String pwd = Utils.enterPassword();
			client.out.println(username);
			client.out.println(pwd);

			response = Utils.readNextLineFromSocket(client.in);
			if(Integer.parseInt(response) != Message.LOGIN_SUCCESS) {
				System.out.println("erreur dans la saisie du mot de passe");
			}
		} while (Integer.parseInt(response) != Message.LOGIN_SUCCESS);

		// Loop to handle image sending and receiving
		while (true) {
			// Get a valid file to send
			File imageToSend = null;
			String filename = "";
			while (imageToSend == null) {
				System.out.println("Please name a file to send");
				filename = Utils.getFileNameFromUser();
				try {
					imageToSend = new File(filename);
					if (!imageToSend.exists()) {
						throw new Exception();
					}
					client.out.println(filename);
				} catch(Exception e) {
					imageToSend = null;
					System.out.println("Wrong filename, please select a valid file");
				}
			}

			// Set modified file name
			System.out.println("Please set a name for the modified file");
			newFilename = Utils.getFileNameFromUser();

			// Send file for modification
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedImage image = ImageIO.read(imageToSend);
				ImageIO.write(image, "jpg", baos);
				baos.flush();
				byte[] imageBytes = baos.toByteArray();
				baos.close();
				client.dos.write(imageBytes, 0, imageBytes.length);

				System.out.println("Image envoyé au serveur pour une modification");
				System.out.println("[" + username +" - "+ serverAddress +":"
						+ port +" - " +
						java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'@'HH:mm:ss"))
						+ "] : Image "+newFilename+" reçue\n" +
						"pour traitement.\n");


			} catch (IOException e) {
				e.printStackTrace();
			}

			// Read data (modified file)
			ByteArrayOutputStream dataContainer = new ByteArrayOutputStream();
			byte[] dataChunk = new byte[Utils.DATA_BUFFER_SIZE];
			int nBytesReceived = 0;
			do {
				nBytesReceived = client.dis.read(dataChunk);
				if (nBytesReceived < 0) {
					throw new IOException();
				}
				dataContainer.write(dataChunk, 0, nBytesReceived);
			} while (nBytesReceived == Utils.DATA_BUFFER_SIZE);

			// Save image locally
			byte[] allData = dataContainer.toByteArray();
			ByteArrayInputStream byis = new ByteArrayInputStream(allData);
			BufferedImage sobelImage = ImageIO.read(byis);
			File image = new File(newFilename);
			ImageIO.write(sobelImage, "jpg", image);
			System.out.println("Avertissement dans la console : l’image traitée a été reçue et sauvegardée à l’emplacement suivant : " + image.getAbsolutePath());

			// Wait for the user to press enter before disconnecting
			System.out.println("Press Enter to disconnect the client for security reasons...");
			System.in.read();

			// Disconnect from the server after processing
			client.disconnect();
			break;
		}
	}
}
