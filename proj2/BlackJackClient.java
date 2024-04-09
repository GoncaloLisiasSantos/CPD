import java.net.*;
import java.io.*;
import java.util.Scanner;

public class BlackjackClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner = new Scanner(System.in);

    public BlackjackClient() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server.");

            // Implement authentication and game queue logic here
            // For simplicity, handle user input/output directly
            while (true) {
                String message = in.readLine();
                System.out.println("Server: " + message);

                if (message.equals("Enter username: ")) {
                    String username = scanner.nextLine();
                    out.println(username);
                } else if (message.equals("Enter password: ")) {
                    String password = scanner.nextLine();
                    out.println(password);
                } else if (message.startsWith("Game starting")) {
                    System.out.println(message);
                    break;
                }
            }

            // Handle game logic here (e.g., receiving game updates)
            // Example: game loop for Blackjack
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new BlackjackClient();
    }
}
