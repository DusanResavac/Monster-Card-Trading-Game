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
    Double Elo,
    Integer GamesPlayed,
    Integer Wins
) implements Serializable
{
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRecord that = (UserRecord) o;

        if (Username != null ? !Username.equals(that.Username) : that.Username != null) return false;
        if (Password != null ? !Password.equals(that.Password) : that.Password != null) return false;
        if (Name != null ? !Name.equals(that.Name) : that.Name != null) return false;
        if (Bio != null ? !Bio.equals(that.Bio) : that.Bio != null) return false;
        if (Image != null ? !Image.equals(that.Image) : that.Image != null) return false;
        if (Coins != null ? !Coins.equals(that.Coins) : that.Coins != null) return false;
        if (Elo != null ? !Elo.equals(that.Elo) : that.Elo != null) return false;
        if (GamesPlayed != null ? !GamesPlayed.equals(that.GamesPlayed) : that.GamesPlayed != null) return false;
        return Wins != null ? Wins.equals(that.Wins) : that.Wins == null;
    }

    @Override
    public int hashCode() {
        int result = Username != null ? Username.hashCode() : 0;
        result = 31 * result + (Password != null ? Password.hashCode() : 0);
        result = 31 * result + (Name != null ? Name.hashCode() : 0);
        result = 31 * result + (Bio != null ? Bio.hashCode() : 0);
        result = 31 * result + (Image != null ? Image.hashCode() : 0);
        result = 31 * result + (Coins != null ? Coins.hashCode() : 0);
        result = 31 * result + (Elo != null ? Elo.hashCode() : 0);
        result = 31 * result + (GamesPlayed != null ? GamesPlayed.hashCode() : 0);
        result = 31 * result + (Wins != null ? Wins.hashCode() : 0);
        return result;
    }

    public HashMap<String, Object> getProperties () {
        return new HashMap<>(){{
            put("username", Username);
            put("password", Password);
            put("name", Name);
            put("bio", Bio);
            put("image", Image);
            put("coins", Coins);
            put("elo",  Elo);
            put("wins", Wins);
            put("gamesPlayed", GamesPlayed);
        }};
    }
}

