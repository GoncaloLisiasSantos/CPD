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

        // Aqui você deve iniciar a lógica do jogo para os dois jogadores
        new Thread(() -> playGame(player1, player2)).start();
    }

    private void playGame(String player1, String player2) {
        // Lógica para iniciar o jogo entre player1 e player2
        // Este método pode ser ajustado para incluir a lógica de envio e recebimento de mensagens entre os jogadores
        MathServer.startGameForPlayers(player1, player2);
    }

    public synchronized int size() {
        return players.size();
    }
}
