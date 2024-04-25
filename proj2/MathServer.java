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
    private static Queue<User> waitingQueue = new LinkedList<>(); // clients waiting in the queue to connect to the
    private static Map<UUID, User> clientsMap = new HashMap<>(); // just to have something that links tokens to clients

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

            // TODO: when connecting to the server, assign a token to the user
            // 1. Assign token to the user
            // 2. Maintain a queue of clients waiting to play the game
            // 3. If a client's connection is lost unexpectedly, keep track of their token
            // and their position in the queue.
            // 4. When a client reconnects, send their token to the server.
            // 5. Upon receiving the token, check if the token corresponds to a client in
            // the waiting queue.
            // 6. If the token matches, resume the client's game session from their saved
            // state.

            while (true) {
                Socket socket = serverSocket.accept();
                clientsLock.lock(); // Lock before modifying the clients list

                try {
                    clients.add(socket);
                } finally {
                    clientsLock.unlock(); // Unlock after modifying the clients list
                }

                handleClientConnection(socket);

                // check if client lost connection
                if (!isConnected(socket)) {
                    // reconnect client
                    reconnectClient(socket);
                    // mark the client as disconnected
                    // ...
                } 
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

    private static void handleClientConnection(Socket socket) {
        UUID clientToken = UUID.randomUUID();
        User user = new User("player1", "password", 100, socket, clientToken);

        // add client to the map
        clientsMap.put(clientToken, user);

        synchronized (waitingQueue) {
            waitingQueue.add(user); // add client to the queue
        }

        new Thread(new ClientHandler(socket, clientToken)).start();
    }

    private static boolean isConnected(Socket socket) {
        return !socket.isClosed();
    }

    // private static void reconnectClient(Socket socket) {
        // compare client's token to the clientsMap 
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private UUID clientToken;

        public ClientHandler(Socket socket, UUID token) {
            this.socket = socket;
            this.clientToken = token;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port or listening for a connection");
                System.out.println(e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Could not close the socket.");
                }
            }
        }
    }
}