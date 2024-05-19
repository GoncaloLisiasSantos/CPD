import java.util.LinkedList;

public class Queue {
    private LinkedList<String> players;

    public Queue() {
        this.players = new LinkedList<>();
    }

    public synchronized void enqueue(String player) {
        players.add(player);
        if (players.size() >= 2) {
            startGame();
        }
    }

    public synchronized String dequeue() {
        if (!players.isEmpty()) {
            return players.removeFirst();
        }
        return null;
    }

    private void startGame() {
        String player1 = players.removeFirst();
        String player2 = players.removeFirst();
        System.out.println("Starting game between " + player1 + " and " + player2);

        Thread.ofVirtual().start(() -> playGame(player1, player2));

    }

    private void playGame(String player1, String player2) {
        MathServer.startGameForPlayers(player1, player2);
    }

    public synchronized int size() {
        return players.size();
    }
}
