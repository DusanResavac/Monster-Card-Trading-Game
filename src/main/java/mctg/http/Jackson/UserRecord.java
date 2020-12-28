package mctg.http.Jackson;

import java.io.Serializable;
import java.util.HashMap;

public record UserRecord (
    String Username,
    String Password,
    String Name,
    String Bio,
    String Image,
    Integer Coins,
    Double Elo
) implements Serializable
{
    public HashMap<String, Object> getProperties () {
        return new HashMap<>(){{
            put("Username", Username);
            put("password", Password);
            put("name", Name);
            put("bio", Bio);
            put("image", Image);
            put("coins", Coins);
            put("elo",  Elo);
        }};
    }
}

