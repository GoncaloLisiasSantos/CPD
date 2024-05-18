import java.io.*;
import java.net.*;
import java.util.*;

public class Player {
  private String username;
  private String passwordHash;
  private Integer highScore;
  protected boolean isLoggedIn;
  private SocketChannel channel;

  public Player(String username, String passwordHash, Integer highScore) {
      this.username = username;
      this.passwordHash = passwordHash;
      this.highScore = highScore;
      private Token player_token ;
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

  public boolean getLoggedIn(){
      return this.isLoggedIn;
  }

  public void setLoggedIn(boolean loggedIn){
      this.isLoggedIn = loggedIn;
  }

  public  SocketChannel getChannel(){
      return this.channel;
  }

  public Token getPlayerToken(){
      return this.player_token;
  }

  public void setToken(Token player_token){
      this.player_token = player_token;
  }

  public void setChannel(SocketChannel channel) {
      this.channel = channel;
  }
  
}
