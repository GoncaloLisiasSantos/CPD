import java.nio.channels.SocketChannel;

public class Player {
  private String username;
  private String passwordHash;
  private Integer highScore;
  protected boolean isLoggedIn;
  private Token player_token;
  private SocketChannel channel;
  private String rank;

  public Player(String username, String passwordHash, Integer highScore, Token token) {
      this.username = username;
      this.passwordHash = passwordHash;
      this.highScore = highScore;
      this.player_token = token;
      this.isLoggedIn = false;
      this.rank  = getRank();
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
      this.rank = getRank();
  }

  public boolean getLoggedIn(){
      return this.isLoggedIn;
  }

  public void setLoggedIn(boolean loggedIn){
      this.isLoggedIn = loggedIn;
  }

  public  SocketChannel getChannel(){
      return this.channel;
  }

  public Token getToken(){
      return this.player_token;
  }

  public void setToken(Token player_token){
      this.player_token = player_token;
  }

  public void setChannel(SocketChannel channel) {
      this.channel = channel;
  }


  public String getRank(){
        if (getHighScore() >= 150) {
            return "Legend";
        } else if (getHighScore() >= 100) {
            return "Gold";
        } else if (getHighScore() >= 30) {
            return "Silver";
        } else {
            return  "Bronze";
        }
    }
  
}
