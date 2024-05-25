package commun;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Utils {
	
	public static final int DATA_BUFFER_SIZE = 1024;
	private static String DEFAULT_IP = "10.200.14.216";
	private static String DEFAULT_PORT = "5005";
	
    private static Scanner scanner = new Scanner(System.in);
    
	public static String getValidAddressFromUser() {
		final Pattern IP_PATTERN = Pattern
				.compile("^(([012]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

		String serverIp = null;
		do {
			System.out.println("Enter server IP address: ");
			serverIp = scanner.nextLine();
			if (serverIp.length() == 0) {
				serverIp = DEFAULT_IP;
			}
		} while (!IP_PATTERN.matcher(serverIp).matches());

		return serverIp;
	}

	public static int getValidPortFromUser() {
		int serverPort = 0;
		do {
			System.out.println("Enter server Port number (5000-5050): ");
			String tmp = scanner.nextLine();
			if (tmp.length() == 0) {
				tmp = DEFAULT_PORT;
			}
			try {
				serverPort = Integer.parseInt(tmp);
			}
			catch (NumberFormatException e) {
				System.out.println("ERROR: invalid port number format -- " + e.getMessage());
			}
			catch (Exception e) {
				System.out.println("ERROR: unknown error -- " + e.getMessage());
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

	
	public static String getStringFromUser() {
		return scanner.nextLine();
	}
	
	
	public static String readNextLineFromSocket(BufferedReader in) throws IOException {
		while (!in.ready()) {}
		return in.readLine();
	}
}
