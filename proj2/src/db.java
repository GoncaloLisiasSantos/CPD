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
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;


public class DatabaseManager {
    private static final Path path = Paths.get("../database.txt");

    private static List<Player> players;

    DatabaseManager() {
        List<Player> players = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader (new FileReader(path.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String passwordHash = parts[1];
                Token token = generateToken(1000);
                int highScore = Integer.parseInt(parts[2]);
                players.add(new Player(username, passwordHash, highScore, token));
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public static boolean authenticate(String username, String passwordHash) {
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
            Files.writeString(path, playerData, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
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
