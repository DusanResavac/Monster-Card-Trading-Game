import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UserTest {

    // TODO: add user tests
    private static User user;

    @BeforeEach
    void beforeAll() {
        user = new User("The Original G", "password");
    }

    @Test
    void createUser() {
        try {
            User user2 = new User ("The Original G", "12345");
            fail("Constructor didn't throw exception, when Exception was expected");
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }

        try {
            User user2 = new User ("Michael Weston", "agent of the year");
        } catch (Exception e) {
            fail("No Exception was expected");
        }
    }

    @Test
    void manageDeck() {

    }


}
