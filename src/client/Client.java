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
    
    Client() {}

	public void connectToServer() throws IOException {

        // Get the server address from a dialog box.
        String serverAddress = Utils.getValidIpFromUser();
        int port = Utils.getValidPortFromUser();
		socket = new Socket(serverAddress, port);
		
        System.out.format("Connected to %s:%d%n", serverAddress, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public static void main(String[] args) throws Exception {
    	 
        Client client = new Client();
        try {
        	client.connectToServer();
        } catch(Exception e) {
        	System.out.println("Error while connecting -- " + e.getMessage());
        }
        
        // Perform login
		String response;
		do {
			String username = Utils.getUsername();
			String pwd = Utils.getPassword();
			client.out.println(username);
			client.out.println(pwd);
		
			response = Utils.readNextLineFromSocket(client.in);
			if(Integer.parseInt(response) != Message.LOGIN_SUCCESS) {
				System.out.println("erreur dans la saisie du mot de passe");
			}
		} while (Integer.parseInt(response) != Message.LOGIN_SUCCESS);
		
		while (true) {
			
			// Get a valid file to send
			File imageToSend = null;
			String filename = "";
			while (imageToSend == null) {
				System.out.println("Please name a file to send");
				filename = Utils.getStringFromUser();
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
			String newFilename = Utils.getStringFromUser();
	       
			// Send file for modification
	        try {
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    		BufferedImage image = ImageIO.read(imageToSend);
	    		ImageIO.write(image, "jpg", baos);
	    		baos.flush();
	            byte[] imageBytes = baos.toByteArray();
		        baos.close();
		        client.dos.write(imageBytes, 0, imageBytes.length);

		        System.out.println("Image sent for modification");
				System.out.println("[" + Utils.getUsername() + " - "+ Utils.getValidIpFromUser()+":"+ Utils.getValidPortFromUser() +" - " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'@'HH:mm:ss")) + "] : Image "+Utils.getStringFromUser()+" reçue\n" +
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
		    System.out.println("avertissement dans la console lorsque l’image traitée a été reçue ainsi que\n" +
					"l’emplacement où elle a été sauvegardée, soit" + newFilename + " dans l'empalcement ...TODO");
			// TODO: 1-Lors de la réception de l’image traitée, le client devra aussi avertir l’utilisateur
			// de cet événement et devra aussi lui indiquer le chemin vers l’image reçue du
			// Serveur

		}
    }
}
