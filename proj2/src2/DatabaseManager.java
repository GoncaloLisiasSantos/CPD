import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.*;

public class DatabaseManager {
    private static final String DATABASE_FILE = "../database.txt";

    private static List<Player> players;

    DatabaseManager() {
        players = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(this.DATABASE_FILE, StandardCharsets.UTF_8);
            for (int i = 2; i < lines.size(); i++) {
                String[] playerData = lines.get(i).split(" - ");
                Player p = new Player(playerData[0], playerData[1], Integer.parseInt(playerData[2]));
                players.add(p);
            }

        } catch (IOException e) {
            System.out.print("Invalid Path");
        }
    }

    public static List<Player> getPlayers() {
        return playerList;
    }
 
    public static Player getPlayer(String username, String passwordHash) {
        for (Player player : players) {
            if (player.getUsername().equals(username) && player.getPasswordHash().equals(passwordHash)) {
                return player;
            }
        }
        return null;
    }

    public  static Player getPlayerByChannel(SocketChannel channel){
        return playerList.stream()
                .filter(player -> {
                    SocketChannel playerChannel = player.getChannel();
                    return playerChannel != null && playerChannel.equals(channel);
                })
                .findAny()
                .orElse(null);
    }

    public boolean authenticate(String username, String passwordHash) {
        boolean playerExists = false;
        for (Player player : players) {
            if  (player.getUsername().equals(username)) {
                playerExists = true;
                if (player.getPasswordHash().equals(passwordHash)) {
                    player.setLoggedIn(true);
                    return true;
                } else {
                    System.out.println("Invalid Password");
                    return false;
                }
            }      
        }
        if (!playerExists) {
            System.out.println("Player does not exist. Please register.");
            return false;
        }
        return false;
    }

    public static String register(String username, String passwordHash) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return false;
            }
        }
        Player newPlayer = new Player(username, passwordHash, 0);
        newPlayer.setLoggedIn(true);
        players.add(newPlayer);
    
        String playerData = "\n" + username + "," + password + "," + 0 ;
        try {
            Files.writeString(this.path, playerData, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.print("Invalid Path");
        }
        return true;
    }

    public void signOutPlayer(String username) {
        boolean playerExists = false;
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                playerExists = true;
                player.setLoggedIn(false);
                player.setChannel(null);
            }
        }
        if (!playerExists) {
            System.out.println("Player does not exist.");
        }
    }
    
}