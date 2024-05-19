import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class MathServer {
    private static ServerSocket serverSocket;
    private static List<Socket> clients = new ArrayList<>();
    private static Map<String, Socket> playerSockets = new HashMap<>();
    private static Lock clientsLock = new ReentrantLock();
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

            while (true) {
                Socket socket = serverSocket.accept();
                clientsLock.lock();
                try {
                    clients.add(socket);
                } finally {
                    clientsLock.unlock();
                }
                Thread.ofVirtual().start(new ClientHandler(socket));
            }
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            e.printStackTrace();
        }
    }

    private static List<String> generateExpressions() {
        List<String> expressions = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int num1 = random.nextInt(20) + 1;
            int num2 = random.nextInt(20) + 1;
            int num3 = random.nextInt(20) + 1;

            String expression;
            if (random.nextBoolean()) {
                String[] operators = { "+", "-" };
                String operator = operators[random.nextInt(operators.length)];
                expression = num1 + " " + operator + " " + num2;
            } else {
                String[] operators = { "+", "-" };
                String operator1 = operators[random.nextInt(operators.length)];
                String operator2 = operators[random.nextInt(operators.length)];
                expression = num1 + " " + operator1 + " " + num2 + " " + operator2 + " " + num3;
            }
            expressions.add(expression);
        }
        return expressions;
    }

    private static List<Integer> generateResults(List<String> expressions) {
        List<Integer> results = new ArrayList<>();
        for (String expression : expressions) {
            results.add(evaluateExpression(expression));
        }
        return results;
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

            List<String> expressions = generateExpressions();
            List<Integer> results = generateResults(expressions);

            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));

            for (int i = 0; i < expressions.size(); i++) {
                out1.println("Question " + (i + 1) + ": " + expressions.get(i));
                out2.println("Question " + (i + 1) + ": " + expressions.get(i));
            }
            out1.println("END_OF_QUESTIONS");
            out2.println("END_OF_QUESTIONS");

            int score1 = 0;
            int score2 = 0;
            int index = 0;
            String inputLine;

            while (index < expressions.size()) {
                // Player 1's turn
                if ((inputLine = in1.readLine()) != null) {
                    try {
                        int answer = Integer.parseInt(inputLine.trim());
                        int correctAnswer = results.get(index);
                        int dif = Math.abs(correctAnswer - answer);
                        if (answer == correctAnswer) {
                            score1 += 10;
                        } else if (dif <= correctAnswer / 10) {
                            score1 += 10 - dif;
                        }
                    } catch (NumberFormatException e) {
                        out1.println("Please enter a valid number.");
                        continue; // Prompt player 1 again for the same question
                    }
                }

                // Player 2's turn
                if ((inputLine = in2.readLine()) != null) {
                    try {
                        int answer = Integer.parseInt(inputLine.trim());
                        int correctAnswer = results.get(index);
                        int dif = Math.abs(correctAnswer - answer);
                        if (answer == correctAnswer) {
                            score2 += 10;
                        } else if (dif <= correctAnswer / 10) {
                            score2 += 10 - dif;
                        }
                    } catch (NumberFormatException e) {
                        out2.println("Please enter a valid number.");
                        continue; // Prompt player 2 again for the same question
                    }
                }

                index++; // Increment index only after both players have answered the current question
            }

            out1.println("Your Score: " + score1);
            out1.println(player2 + " Score: " + score2);
            out2.println("Your Score: " + score2);
            out2.println(player1 + " Score: " + score1);

            dbManager.updateHighScore(player1, score1);
            dbManager.updateHighScore(player2, score2);

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
                    String rank = dbManager.getRank(username);
                   

                    if (authenticationResult) {
                        out.println("AUTH_SUCCESS");
                        String mode = in.readLine();
                        if (mode.equals("SIMPLE_MODE")) {
                            playerSockets.put(username, socket);
                            gameQueue.enqueue(username, rank, 0);
                        } else if (mode.equals("RANKED_MODE")){
                            playerSockets.put(username, socket);
                            gameQueue.enqueue(username, rank, 1);
                        }
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
