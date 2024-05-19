import java.nio.channels.SocketChannel;

public class Player {
    private String username;
    private String passwordHash;
    private Integer highScore;
    private boolean isLoggedIn;
    private Token playerToken;
    private SocketChannel channel;

    public Player(String username, String passwordHash, Integer highScore, Token token) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.highScore = highScore;
        this.playerToken = token;
        this.isLoggedIn = false;
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

    public void setHighScore(Integer highScore) {
        this.highScore = highScore;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public Token getToken() {
        return playerToken;
    }

    public void setToken(Token playerToken) {
        this.playerToken = playerToken;
    }
}
