package commun;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Utils {

	public static final int DATA_BUFFER_SIZE = 1024;
	private static String DEFAULT_IP = "10.200.14.216";
	private static String DEFAULT_PORT = "5005";
	private static String sessionUsername; // Variable to store the username during the session

	private static Scanner scanner = new Scanner(System.in);

	public static String getValidAddressFromUser() {
		final Pattern IP_PATTERN = Pattern
				.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

		String serverIp = null;
		do {
			System.out.println("Enter server IP address: ");
			serverIp = scanner.nextLine();
			if (serverIp.isEmpty()) {
				serverIp = DEFAULT_IP;
			} else if (!IP_PATTERN.matcher(serverIp).matches()) {
				System.out.println("ERROR: Format de votre addresse IP invalide...");
			}
		} while (!IP_PATTERN.matcher(serverIp).matches());

		return serverIp;
	}
	public static int getValidPortFromUser() {
		int serverPort = 0;
		do {
			System.out.println("Enter server Port number (5000-5050): ");
			String tmp = scanner.nextLine();
			if (tmp.isEmpty()) {
				tmp = DEFAULT_PORT;
			}
			try {
				serverPort = Integer.parseInt(tmp);
				if (serverPort < 5000 || serverPort > 5050) {
					System.out.println("ERROR: Port number must be between 5000 and 5050.");
					serverPort = 0; // Reset to loop again
				}
			} catch (NumberFormatException e) {
				System.out.println("ERROR: Invalid port number format -- " + e.getMessage());
			} catch (Exception e) {
				System.out.println("ERROR: Unknown error -- " + e.getMessage());
			}
		} while (serverPort < 5000 || serverPort > 5050);

		return serverPort;
	}
	public static String enterUsername() {
		System.out.print("Enter username: ");
		return scanner.nextLine();
	}

	public static String enterPassword() {
		System.out.print("Enter password: ");
		return scanner.nextLine();
	}

	public static String getUsername() {
		return scanner.nextLine();
	}

	public static String getFileNameFromUser() {
		return scanner.nextLine();
	}

	public static String readNextLineFromSocket(BufferedReader in) throws IOException {
		while (!in.ready()) {}
		return in.readLine();
	}

	// New method to set the session username
	public static void setSessionUsername(String username) {
		sessionUsername = username;
	}

	// New method to get the session username
	public static String getSessionUsername() {
		return sessionUsername;
	}
}
