import java.io.*;
import java.net.*;
import java.util.*;

public class Player {
  private String username;
  private String passwordHash;
  private Integer highScore;

  public Player(String username, String passwordHash, Integer highScore) {
      this.username = username;
      this.passwordHash = passwordHash;
      this.highScore = highScore;
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
}
