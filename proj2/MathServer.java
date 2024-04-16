import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Random;

public class MathServer {

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                // Generate a random arithmetic expression
                String expression = generateRandomExpression();

                System.out.println("Sending expression to client: " + expression);

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println(expression);

                // Close the socket after sending the expression
                socket.close();
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
}
