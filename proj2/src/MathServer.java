import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

public class MathServer {
    private static ServerSocket serverSocket;
    private static Lock playersLock = new ReentrantLock();
    private static List<Player> waitingPlayers = new ArrayList<>();
    private static List<String> expressions = new ArrayList<>();
    private static List<Integer> results = new ArrayList<>();
    private static DatabaseManager dbManager;

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

                        Player player = new Player(username, passwordHash, 0, DatabaseManager.generateToken(1000));
                        player.setChannel(socket.getChannel());
                        player.setLoggedIn(true);
                        
                        playersLock.lock();
                        try {
                            waitingPlayers.add(player);
                            if (waitingPlayers.size() >= 2) {
                                Player player1 = waitingPlayers.remove(0);
                                Player player2 = waitingPlayers.remove(0);
                                new Thread(new GameInstance(player1, player2)).start();
                            }
                        } finally {
                            playersLock.unlock();
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

    private static class GameInstance implements Runnable {
        private Player player1;
        private Player player2;

        public GameInstance(Player player1, Player player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public void run() {
            try {
                SocketChannel channel1 = player1.getChannel();
                SocketChannel channel2 = player2.getChannel();

                if (channel1 == null || channel2 == null) {
                    // One of the players disconnected, end game instance
                    return;
                }

                PrintWriter out1 = new PrintWriter(channel1.socket().getOutputStream(), true);
                PrintWriter out2 = new PrintWriter(channel2.socket().getOutputStream(), true);
                BufferedReader in1 = new BufferedReader(new InputStreamReader(channel1.socket().getInputStream()));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(channel2.socket().getInputStream()));

                out1.println("GAME_START");
                out2.println("GAME_START");

                playGame(out1, in1, out2, in2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void playGame(PrintWriter out1, BufferedReader in1, PrintWriter out2, BufferedReader in2) throws IOException {
            try {
                for (int i = 0; i < expressions.size(); i++) {
                    out1.println("Question " + (i + 1) + ": " + expressions.get(i));
                    out2.println("Question " + (i + 1) + ": " + expressions.get(i));
                }
                out1.println("END_OF_QUESTIONS");
                out2.println("END_OF_QUESTIONS");

                int score1 = 0;
                int score2 = 0;
                int index = 0;

                while (index < expressions.size()) {
                    String input1 = in1.readLine();
                    String input2 = in2.readLine();

                    if (input1 != null && input2 != null) {
                        try {
                            int answer1 = Integer.parseInt(input1.trim());
                            int answer2 = Integer.parseInt(input2.trim());

                            int correctAnswer = results.get(index);
                            int dif1 = Math.abs(correctAnswer - answer1);
                            int dif2 = Math.abs(correctAnswer - answer2);

                            if (answer1 == correctAnswer) {
                                score1 += 10;
                            } else if (dif1 <= correctAnswer / 10) {
                                score1 += 10 - dif1;
                            }

                            if (answer2 == correctAnswer) {
                                score2 += 10;
                            } else if (dif2 <= correctAnswer / 10) {
                                score2 += 10 - dif2;
                            }

                            index++;
                        } catch (NumberFormatException e) {
                            out1.println("Please enter a valid number.");
                            out2.println("Please enter a valid number.");
                        }
                    } else {
                        break;
                    }
                }

                out1.println("GAME_OVER. Your score: " + score1);
                out2.println("GAME_OVER. Your score: " + score2);

            } finally {
                player1.setLoggedIn(false);
                player2.setLoggedIn(false);
                player1.setChannel(null);
                player2.setChannel(null);
            }
        }
    }
}
