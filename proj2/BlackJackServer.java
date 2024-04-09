import java.net.*;
import java.io.*;
import java.util.*;

public class BlackjackServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<Game> games = new ArrayList<>();
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public BlackjackServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void matchPlayers() {
        // Logic to match players into game sessions
        // For simplicity, we can create a new game when enough players are available
        if (clients.size() >= 2) {
            List<ClientHandler> gamePlayers = clients.subList(0, 2); // Example: 2 players per game
            Game game = new Game(gamePlayers);
            games.add(game);
            gamePlayers.forEach(p -> p.setGame(game));
            clients.removeAll(gamePlayers);
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        new BlackjackServer();
    }
}
