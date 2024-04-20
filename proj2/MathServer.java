import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap; 
import java.util.concurrent.atomic.AtomicInteger;

public class MathServer {
    private static List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    private static List<String> expressions = new ArrayList<>();
    private static List<Integer> results = new ArrayList<>();
    private static AtomicInteger currentIndex = new AtomicInteger(0);
    private static Map<Socket, Integer> scores = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            // Generate 10 random expressions
            generateExpressions();

            while (true) {
                Socket socket = serverSocket.accept();
                synchronized (clients) {
                    clients.add(socket);
                }
                scores.put(socket, 0);

                // Start a new thread to handle the client connection
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();

                // Broadcast the current expression to all connected clients if this is the first client
                if (clients.size() == 1) {
                    broadcast(currentIndex + ": " + expressions.get(currentIndex.get()));
                }
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
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
                String[] operators = {"+", "-", "*", "/"};
                String operator = operators[random.nextInt(operators.length)];
                expression = num1 + " " + operator + " " + num2;
            } else {
                String[] operators = {"+", "-", "*"};
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
        int num1 = Integer.parseInt(parts[0]);
        String operator1 = parts[1];
        int num2 = Integer.parseInt(parts[2]);

        int result = 0;
        switch (operator1) {
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
        synchronized (clients) {
            Iterator<Socket> iterator = clients.iterator();
            while (iterator.hasNext()) {
                Socket client = iterator.next();
                try {
                    OutputStream output = client.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println(message);
                } catch (IOException ex) {
                    System.out.println("Error broadcasting message to client: " + ex.getMessage());
                    iterator.remove(); // Remove the client if there's an error
                }
            }
        }
    }

    private static void checkAndEndGame() {
        if (currentIndex.get() >= 10) {
            broadcast("Game Over! Scores: " );
            for (Map.Entry<Socket, Integer> entry : scores.entrySet()) {
                broadcast("Player " + entry.getKey().getPort() + ": " + entry.getValue() + " points");
            }
            resetGame();
        }
    }

    private static void resetGame() {
        currentIndex.set(0);
        expressions.clear();
        results.clear();
        scores.clear();
        generateExpressions();
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
                    String clientGuess = reader.readLine();
                    if (clientGuess == null) break; // Handle client disconnection

                    int guess = Integer.parseInt(clientGuess);

                    if (currentIndex.get() < 10 && guess == results.get(currentIndex.get())) {
                        scores.put(socket, scores.get(socket) + 2); // Increment score by 2 points for correct answer
                    }

                    if (currentIndex.incrementAndGet() < 10) {
                        broadcast(currentIndex + ": " + expressions.get(currentIndex.get()));
                    } else {
                        checkAndEndGame();
                        break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Error handling client input: " + ex.getMessage());
            } finally {
                try {
                    socket.close(); // Ensure the socket is closed on termination
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}
