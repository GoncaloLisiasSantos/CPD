import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;

public class DatabaseManager {
    private static final Path path = Paths.get("../database.txt");
    private static List<Player> players = new ArrayList<>();

    public DatabaseManager() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }
                String username = parts[0];
                String passwordHash = parts[1];
                int highScore = Integer.parseInt(parts[2]);
                Token token = generateToken(1800);
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

    public static Player getPlayerByChannel(SocketChannel channel) {
        return players.stream()
                .filter(player -> {
                    SocketChannel playerChannel = player.getChannel();
                    return playerChannel != null && playerChannel.equals(channel);
                })
                .findAny()
                .orElse(null);
    }

    public static boolean authenticate(String username, String passwordHash) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                if (player.getPasswordHash().equals(passwordHash)) {
                    Token player_token = player.getToken();
                    if (player_token == null || player_token.isExpired()) {
                        player_token = generateToken(1800);
                        tokenFile(username, player_token.get_identifier());
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
        System.out.println("Player does not exist. Please register.");
        return false;
    }

    public static boolean register(String username, String passwordHash) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return false; // Username already exists
            }
        }
        Player newPlayer = new Player(username, passwordHash, 0, null);
        newPlayer.setLoggedIn(true);
        players.add(newPlayer);

        String playerData = username + "," + passwordHash + "," + 0 + "\n";
        try {
            Files.writeString(path, playerData, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.print("Invalid Path");
            e.printStackTrace();
        }
        return true;
    }

    public static Token generateToken(int expiryTime) {
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

        String directoryPath = "Tokens";
        
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                System.out.println("Failed to create directory: " + directoryPath);
                e.printStackTrace();
                return;
            }
        }
        
        try (FileWriter writer = new FileWriter(directoryPath +"/token_" + username + ".txt")) {
            writer.write(token);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void signOutPlayer(String username) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                player.setLoggedIn(false);
                player.setChannel(null);
                return;
            }
        }
        System.out.println("Player does not exist.");
    }

    public static String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }


    public boolean updateHighScore(String username, int newHighScore) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                if (newHighScore > player.getHighScore()) {
                    player.setHighScore(newHighScore);
                    updateDatabaseFile();
                    return true;
                }
                return false; 
            }
        }
        return false;
    }

    private void updateDatabaseFile() {
        try (FileWriter writer = new FileWriter(path.toString())) {
            for (Player player : players) {
                String playerData = player.getUsername() + "," + player.getPasswordHash() + "," + player.getHighScore() + "\n";
                writer.write(playerData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLeaderboard() {
        StringBuilder leaderboard = new StringBuilder();
        players.sort(Comparator.comparing(Player::getHighScore).reversed());

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            leaderboard.append((i + 1)).append(". ")
                        .append(player.getUsername())
                        .append(": ")
                        .append(player.getHighScore())
                        .append("\n");
        }
        return leaderboard.toString();
    }


}
