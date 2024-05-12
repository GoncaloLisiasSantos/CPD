import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class MathServer {
    private static ServerSocket serverSocket;
    private static List<Socket> clients = new ArrayList<>();
    private static Lock clientsLock = new ReentrantLock(); // Lock for managing clients list
    private static List<String> expressions = new ArrayList<>();
    private static List<Integer> results = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MathServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            generateExpressions();

            while (true) {
                Socket socket = serverSocket.accept();
                clientsLock.lock(); // Lock before modifying the clients list
                try {
                    clients.add(socket);
                } finally {
                    clientsLock.unlock(); // Unlock after modifying the clients list
                }
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            e.printStackTrace();
        }
    }

    private static void generateExpressions() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int num1 = random.nextInt(10) + 1;
            int num2 = random.nextInt(10) + 1;
            int num3 = random.nextInt(10) + 1;

            String expression;
            if (random.nextBoolean()) {
                String[] operators = { "+", "-", "*", "/" };
                String operator = operators[random.nextInt(operators.length)];
                expression = num1 + " " + operator + " " + num2;
            } else {
                String[] operators = { "+", "-", "*" };
                String operator1 = operators[random.nextInt(operators.length)];
                String operator2 = operators[random.nextInt(operators.length)];
                expression = num1 + " " + operator1 + " " + num2 + " " + operator2 + " " + num3;
            }
            expressions.add(expression);
            results.add(evaluateExpression(expression));
        }
    }

    private static int evaluateExpression(String expression) {
        String[] parts = expression.split(" ");
        int result = Integer.parseInt(parts[0]);

        for (int i = 1; i < parts.length; i += 2) {
            String operator = parts[i];
            int nextOperand = Integer.parseInt(parts[i + 1]);

            switch (operator) {
                case "+":
                    result += nextOperand;
                    break;
                case "-":
                    result -= nextOperand;
                    break;
                case "*":
                    result *= nextOperand;
                    break;
                case "/":
                    if (nextOperand != 0)
                        result /= nextOperand;
                    break;
            }
        }
        return result;
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String command = in.readLine(); // Read the command from the client
                System.out.println("Command received: " + command);
                if ("REGISTER".equals(command)) { // Check if the command is for registration
                    String username = in.readLine();
                    String passwordHash = hashPassword(in.readLine());

                    // Register the user using DatabaseManager
                    String registrationResult = DatabaseManager.register(username, passwordHash);

                    if ("REG_SUCCESS".equals(registrationResult)) {
                        out.println("REG_SUCCESS"); // Notify the client about successful registration
                    } else {
                        out.println("REG_FAIL"); // Notify the client about registration failure
                    }
                } else { // Handle authentication for other commands
                    System.out.println("Command received: " + command);
                    String username = in.readLine();
                    String passwordHash = hashPassword(in.readLine());

                    // Authenticate the user using DatabaseManager
                    String authenticationResult = DatabaseManager.authenticate(username, passwordHash);

                    if ("AUTH_SUCCESS".equals(authenticationResult)) {
                        out.println("AUTH_SUCCESS");
                        // Proceed with the game logic
                        playGame(out, in);
                    } else {
                        out.println("AUTH_FAIL");
                        socket.close(); // Close the connection
                    }
                }

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port or listening for a connection");
                System.out.println(e.getMessage());
            }
        }

        private void playGame(PrintWriter out, BufferedReader in) throws IOException {
            try {
                for (int i = 0; i < expressions.size(); i++) {
                    out.println("Question " + (i + 1) + ": " + expressions.get(i));
                }
                out.println("END_OF_QUESTIONS");

                String inputLine;
                int score = 0;
                int index = 0;
                while ((inputLine = in.readLine()) != null) {
                    try {
                        int answer = Integer.parseInt(inputLine.trim());
                        if (answer == results.get(index)) {
                            score += 2;
                        }
                        index++;
                        if (index >= expressions.size())
                            break;
                    } catch (NumberFormatException e) {
                        out.println("Please enter a valid number.");
                    }
                }

                out.println("Your score: " + score);
            } finally {
                socket.close(); // Close the connection after the game
            }
        }

        private static String hashPassword(String password) {
            // Implement password hashing algorithm (e.g., SHA-256)
            // Return hashed password
            return password;
        }
    }

}