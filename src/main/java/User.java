import lombok.Getter;
import lombok.Setter;

public class User {

    @Getter
    private String username;
    @Getter
    private String password;

    public User(String username, String password) {

    }

    public void setPassword (String password) {
        // TODO: hash password and save it in this.password
    }


}
