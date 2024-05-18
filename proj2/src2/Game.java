import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Game implements Runnable {

    private final ArrayList<String> expressions;
    private final ArrayList<String> results;

    private final Queue queue;

    private Selector selector;

    private boolean gameOver;

    private final ArrayList<Player> players;
    private static final int ELO_INCREASE = 100;
    private static final int ELO_DECREASE = 50;

    private final Set<SocketChannel> readyPlayers = new HashSet<>();
    private final Set<SocketChannel> disconnectedPlayers = new HashSet<>();

    public Game(ArrayList<Player> players, Queue queue) {
        this.players = players;
        this.expressions = generateExpressions();
        this.results = evaluateExpressions(expressions);
        this.gameOver = false;
        this.queue =  queue;
    }

    @Override
    public void run() {
        System.out.println("Game started");
        try {
            Selector selector = Selector.open();
            this.selector = selector;
            registerSocketChannels(selector);

            while(disconnectedPlayers.size() != players.size()){
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(!key.isValid()){
                        continue;
                    }
                    if(key.isReadable()){
                        read(key);
                    }
                }
            }
            Server.currentGames.remove(this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private ArrayList<String> generateExpressions() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int num1 = random.nextInt(50) + 1;
            int num2 = random.nextInt(50) + 1;
            int num3 = random.nextInt(50) + 1;
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
        }
        return expressions;
    }

    private ArrayList<String> evaluateExpressions(ArrayList<String> expressions) {
        for (String expression : expressions) {
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
            results.add(String.valueOf(result));
        }
        return results;
    }
}