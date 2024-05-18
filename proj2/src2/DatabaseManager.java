import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.io.FileWriter;
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
                Token token = generateToken(1000);
                Player p = new Player(playerData[0], playerData[1], Integer.parseInt(playerData[2]), token);
                players.add(p);
            }

        } catch (IOException e) {
            System.out.print("Invalid Path");
        }
    }


    public static List<Player> getPlayers() {
        return players;
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
        return players.stream()
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
                // Check if the player's token has expired
                Token player_token = player.getToken();
                if (player_token == null || player_token.isExpired()) {
                    player_token = generateToken(1800); // Generate a new player_token with 30 minutes expiry
                    player.setToken(player_token);
                }
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

    public static Boolean register(String username, String passwordHash) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return false;
            }
        }
        Player newPlayer = new Player(username, passwordHash, 0, null);
        newPlayer.setLoggedIn(true);
        players.add(newPlayer);
    
        String playerData = "\n" + username + "," + passwordHash + "," + 0 + "";
        try {
            Files.writeString(this.DATABASE_FILE, playerData, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.print("Invalid Path");
        }
        return true;
    }

    public static Token generateToken (int expiryTime ){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = 10;

        Random random = new Random();
        StringBuilder new_token = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            char c = chars.charAt(index);
            new_token.append(c);
        }

        String aux = new_token.toString();

        LocalDateTime expiry = LocalDateTime.now().plusSeconds(expiryTime);
        
        return new Token(aux, expiry);
    }

    public static void tokenFile(String username, String token) {
        try (FileWriter writer = new FileWriter("token"+username+".txt")) {
            writer.write(token);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();  
    }
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
