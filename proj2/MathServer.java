import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



public class MathServer {
    private static List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    private static Map<Socket, Integer> scores = new ConcurrentHashMap<>();
    private static ServerSocket serverSocket;
    private static volatile boolean serverRunning = false;
    private static List<String> expressions = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        // Prepare the server thread but do not start it immediately
        Thread serverThread = prepareServerThread(port);

        // Menu loop
        while (!exit) {
            System.out.println("1. Play");
            System.out.println("2. Display High Scores");
            System.out.println("3. Exit");

            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    if (!serverRunning && !serverThread.isAlive()) {
                        serverThread = prepareServerThread(port); // Prepare a new thread if the old one has finished running
                        serverThread.start();
                    } else {
                        System.out.println("Server already running.");
                    }
                    break;
                case 2:
                    displayHighScores();
                    break;
                case 3:
                    exit = true;
                    if (serverRunning && !serverSocket.isClosed()) {
                        try {
                            serverSocket.close(); // Stops the server
                        } catch (IOException e) {
                            System.out.println("Error closing server: " + e.getMessage());
                        }
                    }
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        scanner.close();
        System.exit(0);
    }

    private static Thread prepareServerThread(int port) {
        return new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                serverRunning = true;
                System.out.println("Server is listening on port " + port);
                generateExpressions();

                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    synchronized (clients) {
                        clients.add(socket);
                    }
                    scores.put(socket, 0);
                    Thread clientThread = new Thread(new ClientHandler(socket));
                    clientThread.start();
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                serverRunning = false;
            }
        });
    }

    private static void displayHighScores() {
        System.out.println("High Scores:");
        scores.entrySet().stream()
              .sorted(Map.Entry.<Socket, Integer>comparingByValue().reversed())
              .forEach(e -> System.out.println(e.getKey().getInetAddress().getHostAddress() + ": " + e.getValue()));
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

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                // Send all questions immediately after connection
                for (int i = 0; i < expressions.size(); i++) {
                    String question = "Question " + (i + 1) + ": " + expressions.get(i);
                    writer.println(question);
                }

                // Process responses
                int questionIndex = 0;
                while (questionIndex < expressions.size()) {
                    String response = reader.readLine();
                    if (response == null) break; // Client disconnected

                    int answer = Integer.parseInt(response.trim());
                    int correctAnswer = Integer.parseInt(expressions.get(questionIndex).split(" \\+ ")[1]) + 
                                        Integer.parseInt(expressions.get(questionIndex).split(" \\+ ")[0]);

                    if (answer == correctAnswer) {
                        scores.put(socket, scores.getOrDefault(socket, 0) + 2);
                    }

                    questionIndex++;
                }

                writer.println("Game Over! Your score: " + scores.get(socket));

            } catch (IOException ex) {
                System.out.println("Client disconnected: " + ex.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}
