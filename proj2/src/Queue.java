import java.util.LinkedList;

public class Queue {
    private LinkedList<String> players;

    public Queue() {
        this.players = new LinkedList<>();
    }

    // Adiciona um jogador à fila
    public synchronized void enqueue(String player) {
        players.add(player);
        // Verifica se há pelo menos dois jogadores na fila para iniciar o jogo
        if (players.size() >= 2) {
            startGame();
        }
    }

    // Remove um jogador da fila
    public synchronized String dequeue() {
        if (!players.isEmpty()) {
            return players.removeFirst();
        }
        return null;
    }

    // Inicia o jogo com os dois primeiros jogadores da fila
    private void startGame() {
        String player1 = players.removeFirst();
        String player2 = players.removeFirst();
        System.out.println("Starting game between " + player1 + " and " + player2);
    }

    public synchronized int size() {
        return players.size();
    }
}
