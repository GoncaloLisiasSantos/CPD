import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

public class Queue {
    private final LinkedList<String> regularQueue;
    private final Map<String, LinkedList<String>> rankQueues;


    public Queue() {
        this.regularQueue = new LinkedList<>();
        this.rankQueues = new HashMap<>();
        // Initialize queues for each rank
        rankQueues.put("Bronze", new LinkedList<>());
        rankQueues.put("Silver", new LinkedList<>());
        rankQueues.put("Gold", new LinkedList<>());
        rankQueues.put("Legend", new LinkedList<>());
    }

    public synchronized void enqueue(String player, String rank, Integer mode) {
        if (mode == 0) {
            regularQueue.add(player);
            if (regularQueue.size() >= 2) {
                startGame("simple");
            }
        } else if (mode == 1) {
            LinkedList<String> queue = rankQueues.get(rank);
            if (queue != null) {
                queue.add(player);
                if (queue.size() >= 2) {
                    startGame(rank);
                }
            }
        }
    }

    public synchronized String dequeue(String rank) {
        LinkedList<String> queue = rankQueues.get(rank);
        if (queue != null && !queue.isEmpty()) {
            return queue.removeFirst();
        }
        return null;
    }

    private void startGame(String rank) {
        if (rank.equals("simple")) {
            String player1 = regularQueue.removeFirst();
            String player2 = regularQueue.removeFirst();
            System.out.println("Starting game between " + player1 + " and " + player2 + " in simple mode");

            Thread.ofVirtual().start(() -> playGame(player1, player2));
        } else { 
            LinkedList<String> queue = rankQueues.get(rank);
            if (queue != null && queue.size() >= 2) {
                String player1 = queue.removeFirst();
                String player2 = queue.removeFirst();
                System.out.println("Starting game between " + player1 + " and " + player2 + " in rank " + rank);

                Thread.ofVirtual().start(() -> playGame(player1, player2));
            }
        }

    }

    private void playGame(String player1, String player2) {
        MathServer.startGameForPlayers(player1, player2);
    }

    public synchronized int size(String rank) {
        LinkedList<String> queue = rankQueues.get(rank);
        if (queue != null) {
            return queue.size();
        }
        return 0;
    }

    public synchronized Map<String, Integer> size() {
        Map<String, Integer> sizes = new HashMap<>();
        for (Map.Entry<String, LinkedList<String>> entry : rankQueues.entrySet()) {
            sizes.put(entry.getKey(), entry.getValue().size());
        }
        return sizes;
    }
}
