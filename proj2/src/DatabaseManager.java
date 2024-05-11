import java.io.*;
import java.util.*;

public class DatabaseManager {
    private static final String DATABASE_FILE = "../database.txt";

    public static List<Player> readPlayers() {
        List<Player> players = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String passwordHash = parts[1];
                int highScore = Integer.parseInt(parts[2]);
                players.add(new Player(username, passwordHash, highScore));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return players;
    }
    public static String authenticate(String username, String passwordHash) {
        List<Player> players = readPlayers();
        for (Player player : players) {
            if (player.getUsername().equals(username) && player.getPasswordHash().equals(passwordHash)) {
                return "AUTH_SUCCESS"; // Authentication successful
            }
        }
        return "AUTH_FAIL"; // Authentication failed
    }
}