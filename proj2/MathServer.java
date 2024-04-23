import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap; 

public class MathServer {
    private static List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    private static List<String> expressions = new ArrayList<>();
    private static List<Integer> results = new ArrayList<>();
    private static Map<Socket, Integer> scores = new ConcurrentHashMap<>();
    private static Map<Socket, Boolean> hasAnswered = new ConcurrentHashMap<>();


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
                result /= nextOperand;
                break;
        }
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


    private static void resetGame() {
        //currentIndex.set(0);
        expressions.clear();
        results.clear();
        scores.clear();
        generateExpressions();
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private int currentQuestionIndex = 0; // Novo índice para cada cliente

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

                // Enviar todas as perguntas imediatamente após a conexão
                for (String expr : expressions) {
                    writer.println(expr);
                }

                // Loop para lidar com as respostas
                while (currentQuestionIndex < expressions.size()) {
                    String clientGuess = reader.readLine();
                    if (clientGuess == null) break; // Tratar desconexão

                    int guess = Integer.parseInt(clientGuess);
                    int correctAnswer = results.get(currentQuestionIndex);

                    if (guess == correctAnswer) {
                        scores.put(socket, scores.get(socket) + 2); // Incrementar pontuação
                    }

                    currentQuestionIndex++; // Incrementar índice para este cliente
                }

                // Enviar pontuação final para o cliente
                writer.println("Game Over! Your score: " + scores.get(socket));

            } catch (IOException ex) {
                System.out.println("Error handling client input: " + ex.getMessage());
            } finally {
                try {
                    socket.close(); // Fechar o socket ao terminar
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

}
