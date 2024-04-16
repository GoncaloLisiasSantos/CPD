import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class MathServer {
    private static List<Socket> clients = new ArrayList<>();
    private static String currentExpression;
    private static int currentResult;

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                clients.add(socket);

                // Start a new thread to handle the client connection
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();

                // Generate a new random expression if this is the first client
                if (clients.size() == 1) {
                    currentExpression = generateRandomExpression();
                    currentResult = evaluateExpression(currentExpression);
                    System.out.println("New expression generated: " + currentExpression);
                }

                // Broadcast the current expression to all connected clients
                broadcast(currentExpression);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String generateRandomExpression() {
        Random random = new Random();
        int num1 = random.nextInt(10) + 1; // Generate random number between 1 and 10
        int num2 = random.nextInt(10) + 1;
        String[] operators = {"+", "-", "*", "/"};
        String operator = operators[random.nextInt(operators.length)];

        return num1 + " " + operator + " " + num2;
    }

    private static int evaluateExpression(String expression) {
        String[] parts = expression.split(" ");
        int num1 = Integer.parseInt(parts[0]);
        String operator = parts[1];
        int num2 = Integer.parseInt(parts[2]);

        int result = 0;
        switch (operator) {
            case "+":
                result = num1 + num2;
                break;
            case "-":
                result = num1 - num2;
                break;
            case "*":
                result = num1 * num2;
                break;
            case "/":
                result = num1 / num2;
                break;
        }
        return result;
    }

    private static void broadcast(String message) {
        for (Socket client : clients) {
            try {
                OutputStream output = client.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(message);
            } catch (IOException ex) {
                System.out.println("Error broadcasting message to client: " + ex.getMessage());
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                while (true) {
                    // Wait for client's guess
                    String clientGuess = reader.readLine();
                    int guess = Integer.parseInt(clientGuess);

                    // Compare client's guess with the current result
                    if (guess == currentResult) {
                        broadcast("Player " + socket.getPort() + " wins!");
                        break; // End the game
                    }
                }
            } catch (IOException ex) {
                System.out.println("Error handling client input: " + ex.getMessage());
            }
        }
    }
}
