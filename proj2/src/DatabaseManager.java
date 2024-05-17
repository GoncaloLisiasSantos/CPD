import java.io.*;
import java.util.*;

public class DatabaseManager {
    private static final String DATABASE_FILE = "../database.txt";

    // get player 
    public static Player getPlayer(String username, String passwordHash) {
        List<Player> players = readPlayers();
        for (Player player : players) {
            if (player.getUsername().equals(username) && player.getPasswordHash().equals(passwordHash)) {
                return player;
            }
        }
        return null;
    }

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

    // register a new player
    public static String register(String username, String passwordHash) {
        // Check if the username already exists
        List<Player> players = readPlayers();
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                System.out.println("Username already exists. Please choose a different username.");
                return "REG_FAIL";
            }
        }
    
        // Add the new player to the database
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATABASE_FILE, true))) {
            writer.write(username + "," + passwordHash + ",0\n");
            return "REG_SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            return "REG_FAIL";
        }
    }
    
}