package mctg;

import java.io.Serializable;
import java.util.HashMap;

public record UserRecord (
    String username,
    String password,
    String name,
    String bio,
    String image
) implements Serializable
{
    public HashMap<String, String> getProperties () {
        return new HashMap<>(){{
            put("username", username);
            put("password", password);
            put("name", name);
            put("bio", bio);
            put("image", image);
        }};
    }
}

