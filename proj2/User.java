import java.net.Socket;
import java.util.UUID;

public class User {
    private String username;
    private String passwordHash;
    private Integer highScore;
    private Socket socket;
    private UUID token;

    public User(String username, String passwordHash, Integer highScore, Socket socket, UUID token) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.highScore = highScore;
        this.socket = socket;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Integer getHighScore() {
        return highScore;
    }

    public Socket getSocket() {
        return socket;
    }
    public UUID getToken() {
        return token;
    }
}