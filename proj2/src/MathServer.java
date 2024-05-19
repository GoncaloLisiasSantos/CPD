import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class MathServer {
    private static ServerSocket serverSocket;
    private static List<Socket> clients = new ArrayList<>();
    private static Map<String, Socket> playerSockets = new HashMap<>();
    private static Lock clientsLock = new ReentrantLock();
    private static List<String> expressions = new ArrayList<>();
    private static List<Integer> results = new ArrayList<>();
    private static DatabaseManager dbManager;
    private static Queue gameQueue = new Queue();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MathServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            dbManager = new DatabaseManager();

            generateExpressions();

            while (true) {
                Socket socket = serverSocket.accept();
                clientsLock.lock();
                try {
                    clients.add(socket);
                } finally {
                    clientsLock.unlock();
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

    public static void startGameForPlayers(String player1, String player2) {
        try {
            Socket socket1 = getPlayerSocket(player1);
            Socket socket2 = getPlayerSocket(player2);

            if (socket1 == null || socket2 == null) {
                return;
            }

            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));

            // Enviar perguntas e processar respostas para os jogadores
            for (int i = 0; i < expressions.size(); i++) {
                out1.println("Question " + (i + 1) + ": " + expressions.get(i));
                out2.println("Question " + (i + 1) + ": " + expressions.get(i));
            }
            out1.println("END_OF_QUESTIONS");
            out2.println("END_OF_QUESTIONS");

            int score1 = 0;
            int score2 = 0;
            String inputLine;

            for (int i = 0; i < expressions.size(); i++) {
                if ((inputLine = in1.readLine()) != null) {
                    try {
                        int answer = Integer.parseInt(inputLine.trim());
                        if (answer == results.get(i)) {
                            score1 += 2;
                        }
                    } catch (NumberFormatException e) {
                        out1.println("Please enter a valid number.");
                    }
                }

                if ((inputLine = in2.readLine()) != null) {
                    try {
                        int answer = Integer.parseInt(inputLine.trim());
                        if (answer == results.get(i)) {
                            score2 += 2;
                        }
                    } catch (NumberFormatException e) {
                        out2.println("Please enter a valid number.");
                    }
                }
            }

            out1.println("Your score: " + score1);
            out2.println("Your score: " + score2);

            socket1.close();
            socket2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Socket getPlayerSocket(String player) {
        return playerSockets.get(player);
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
                if ("REGISTER".equals(command)) {
                    String username = in.readLine();
                    String passwordHash = DatabaseManager.hashPassword(in.readLine());

                    boolean registrationResult = dbManager.register(username, passwordHash);

                    if (registrationResult) {
                        out.println("REG_SUCCESS");
                    } else {
                        out.println("REG_FAIL");
                    }
                } else if ("LOGIN".equals(command)) {
                    String username = in.readLine();
                    String passwordHash = DatabaseManager.hashPassword(in.readLine());

                    boolean authenticationResult = dbManager.authenticate(username, passwordHash);

                    if (authenticationResult) {
                        out.println("AUTH_SUCCESS");
                        playerSockets.put(username, socket);
                        gameQueue.enqueue(username);
                    } else {
                        out.println("AUTH_FAIL");
                        socket.close();
                    }
                } else {
                    out.println("INVALID_COMMAND");
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
