import java.io.*;
import java.net.*;

public class MathClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java MathClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Play");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            try {
                String choice = userInputReader.readLine();
                if ("2".equals(choice)) {
                    System.out.println("Exiting client.");
                    break;
                } else if ("1".equals(choice)) {
                    System.out.print("Enter username: ");
                    String username = userInputReader.readLine();
                    System.out.print("Enter password: ");
                    String password = userInputReader.readLine();

                    authenticateAndPlay(hostname, port, username, password);
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            } catch (IOException e) {
                System.out.println("Error reading input from user: " + e.getMessage());
            }
        }
    }

    private static void authenticateAndPlay(String hostname, int port, String username, String password) {
        try (Socket socket = new Socket(hostname, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // Send username and password to server for authentication
            writer.println(username);
            writer.println(password);

            // Receive authentication response from server
            String response = reader.readLine();

            if ("AUTH_SUCCESS".equals(response)) {
                System.out.println("Authentication successful. Connected to the server.");
                playGame(reader, writer);
            } else {
                System.out.println("Authentication failed. Please try again.");
            }

        } catch (UnknownHostException e) {
            System.out.println("Don't know about host " + hostname);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + hostname);
            e.printStackTrace();
        }
    }

    private static void playGame(BufferedReader reader, PrintWriter writer) throws IOException {
        System.out.println("Ready to answer questions!");
        String fromServer;
        while ((fromServer = reader.readLine()) != null) {
            if ("END_OF_QUESTIONS".equals(fromServer)) {
                break;
            }
            System.out.println(fromServer);

            System.out.print("Your answer: ");
            String userResponse = new BufferedReader(new InputStreamReader(System.in)).readLine();
            writer.println(userResponse);
        }

        // Receive final score
        System.out.println(reader.readLine());
    }
}
