import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
public class ClientHandler extends Thread {

    private Socket socket;
    private int clientNumber;

// 90% du code à coder ici, décomposer et recomposer l'Image en octets

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
    }

    public void run() {
        try {
            // Création du flux pour envoyer un message au client
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hello from server - you are client #" + clientNumber);

            System.out.println("New connection with client#" + clientNumber + " at " + socket);
        } catch (IOException e) {
            System.out.println("Error handling client#" + clientNumber);
        } finally {
            try {
                // Fermeture de la connexion avec le client
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client#" + clientNumber + " closed");
        }
    }
}
