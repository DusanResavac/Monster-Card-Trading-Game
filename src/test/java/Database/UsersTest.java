package Database;

import mctg.UserRecord;
import mctg.database.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsersTest {
    private Database db;

    @BeforeEach
    void beforeEach() {
        db = new Database();
        db.openConnection("jdbc:postgresql://localhost:5432/mctg", "postgres", "password");
    }

    @Test
    @DisplayName("Testing inserts into Users")
    void test001Insert() {
        assertTrue(db.insertUsers(new UserRecord("SmegmaHunterxTrashTaste", "never", null, null, null)));
        assertFalse(db.insertUsers(new UserRecord("SmegmaHunterxTrashTaste", "gonna", null, null, null)));
        assertTrue(db.insertUsers(new UserRecord("SmegmaHunterxTrashTaste2", "rickroll", null, null, null)));
        assertTrue(db.insertUsers(new UserRecord("You", "you", null,"Hey, just looking for 40h Feeder (m,w,d)", "Some Overwatch pic.png")));


        assertTrue(db.insertUsers(new UserRecord("altenhof", "passwort", "Markus Altenhofer", "just chillin'", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB")));
        assertTrue(db.insertUsers(new UserRecord("kienboec", "passwort", "Daniel Kienböck", "just chillin'", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB")));
    }

    @Test
    @DisplayName("Testing login users")
    void test002Login() {
        assertEquals(db.loginUser("altenhof", "passwort"), "altenhof-mtcgToken");
        assertNull(db.loginUser("altenhof", "passwort2"));
        assertNull(db.loginUser("non-existent", "passwort2"));
        assertEquals(db.loginUser("kienboec", "passwort"), "kienboec-mtcgToken");
    }

    @Test
    @DisplayName("Testing Token checking measures users")
    void test003Login() throws InterruptedException {
        assertTrue(db.checkTokenAndUsername("altenhof-mtcgToken", "altenhof"));
        assertFalse(db.checkTokenAndUsername("kienboec-mtcgToken", "altenhof"));

        // while testing this function, I reduced the time the token was valid to 5 seconds. Therefore this statement wouldn't trigger a test error.
        //Thread.sleep(5000);
        //assertFalse(db.checkTokenValidity("altenhof-mtcgToken"));
    }

    @Test
    @DisplayName("Testing update users")
    void test004Update() {
        assertTrue(db.updateUser(new UserRecord(null, "passwort2", null, "just cool lecturer things", null), "altenhof", "altenhof-mtcgToken"));
        assertFalse(db.updateUser(new UserRecord("kienboec", null, null, null, null), "altenhof", "altenhof-mtcgToken"));
    }

}
