import java.io.*;
import java.net.*;

public class MathClient {

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            while (true) {
                // Receive and display the arithmetic expression from server
                String expression = reader.readLine();
                System.out.println("Received expression from server: " + expression);

                // Prompt user to enter their guess
                System.out.print("Enter your guess for the result of the expression: ");
                BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
                String userGuess = userInputReader.readLine();

                // Send user's guess to server
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(userGuess);

                // Receive and display server's response
                String response = reader.readLine();
                System.out.println(response);
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
