import java.io.*;
import java.net.*;

public class MathClient {

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String expression = reader.readLine();
            System.out.println("Received expression from server: " + expression);

            // Evaluate the received arithmetic expression
            int result = evaluateExpression(expression);
            System.out.println("Result of expression: " + result);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
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
}
