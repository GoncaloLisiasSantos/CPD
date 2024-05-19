import java.time.LocalDateTime;

public class Token {
    private String token_id;
    private LocalDateTime expiryTime;

    public Token(String token_id, LocalDateTime expiryTime) {
        this.token_id = token_id;
        this.expiryTime = expiryTime;
    }

    public String get_identifier() {
        return token_id;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isValid() {
        return !isExpired();
    }

    //public void renewExpiryTime(LocalDateTime newExpiryTime) {
    //    this.expiryTime = newExpiryTime;
    //}
}