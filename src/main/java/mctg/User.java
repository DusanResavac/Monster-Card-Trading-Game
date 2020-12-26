package mctg;

import lombok.Getter;
import lombok.Setter;

public class User {

    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String bio;
    @Getter
    @Setter
    private String image;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }




}
