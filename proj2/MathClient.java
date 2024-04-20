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

        try (Socket socket = new Socket(hostname, port)) {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to the server. Ready to answer questions!");

            boolean gameOver = false;

            while (true) {
                String serverMessage = reader.readLine();
                if (serverMessage == null) {
                    break; // Server has closed the connection
                }
                
                System.out.println(serverMessage);

                // If the "Game Over" message is detected, we set a flag but don't break the loop
                if (serverMessage.startsWith("Game Over")) {
                    gameOver = true;
                    continue; 
                }

                // After the "Game Over" message, if a new line is read that doesn't start with "Player", we assume scores are done
                if (gameOver && !serverMessage.startsWith("Player")) {
                    break; // All scores have been received
                }

                // If the game is not over, continue to read guesses from the user
                if (!gameOver) {
                    System.out.print("Enter your guess for the result of the expression: ");
                    BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
                    String userGuess = userInputReader.readLine();
                    writer.println(userGuess); // Send user's guess to the server
                }
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
