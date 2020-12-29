package mctg;

import lombok.Getter;
import lombok.Setter;
import mctg.http.Jackson.UserRecord;

public class BattleEntry {
    @Getter @Setter
    private String token;
    @Getter @Setter
    private UserRecord userRecord;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BattleEntry that = (BattleEntry) o;

        if (!token.equals(that.token)) return false;
        return userRecord.equals(that.userRecord);
    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + userRecord.hashCode();
        return result;
    }

    public BattleEntry (String token, UserRecord userRecord) {
        this.token = token;
        this.userRecord = userRecord;
    }
}
