import mctg.*;
import mctg.http.Jackson.CardRecord;
import mctg.http.Jackson.TradeOfferRecord;
import mctg.http.Jackson.UserRecord;
import mctg.database.Database;
import mctg.monsters.Dragon;
import mctg.monsters.Elf;
import mctg.monsters.Wizard;
import mctg.traps.Lucky;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTest {
    private static Database db;

    @BeforeAll
    static void beforeAll() {
        db = new Database();
        db.openConnection("jdbc:postgresql://localhost:5432/mctg", "postgres", "password");
        db.deleteEveryEntry();
    }

    // Helfermethoden
    private boolean areCardsTheSame (List<Card> cards1, List<Card> cards2) {
        if (cards1.size() != cards2.size()) {
            return false;
        }

        for (Card card1: cards1) {
            boolean cards2ContainsCard1 = false;
            for (Card card2: cards2) {
                if (card2.equals(card1)) {
                    cards2ContainsCard1 = true;
                    break;
                }
            }
            if (!cards2ContainsCard1) {
                return false;
            }
        }
        return true;
    }
    private boolean areCardIdsTheSame(List<String> cardIds1, List<String> cardIds2) {
        for (String cardId: cardIds1) {
            if (!cardIds2.contains(cardId)) {
                return false;
            }
        }
        return true;
    }

    /*
    #####
            USERS
    #####
    */

    @Test
    @DisplayName("Testing inserts into Users")
    @Order(1)
    void testUserInsert() {
        assertTrue(db.insertUsers(new UserRecord("SmegmaHunterxTrashTaste", "never", null, null, null, 20, 100.0, 0, 0)));
        assertFalse(db.insertUsers(new UserRecord("SmegmaHunterxTrashTaste", "gonna", null, null, null, 20, 100.0, 0, 0)));
        assertTrue(db.insertUsers(new UserRecord("TrashTaste", "rickroll", null, null, null, 20, 100.0, 0, 0)));
        assertTrue(db.insertUsers(new UserRecord("You", "you", null,"Hey, just looking for 40h Feeder (m,w,d)", "Some Overwatch pic.png", 20, 100.0, 0, 0)));


        assertTrue(db.insertUsers(new UserRecord("admin", "passwort", null, null, "https://www.memesmonkey.com/images/memesmonkey/8c/8c4fafb301810373c6e37285e9ec7b03.jpeg", 5, null, null, null)));
        assertTrue(db.insertUsers(new UserRecord("altenhof", "passwort", "Markus Altenhofer", "just chillin'", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB", 20, 100.0, 0, 0)));
        assertTrue(db.insertUsers(new UserRecord("kienboec", "passwort", "Daniel Kienböck", "just chillin'", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB", 20, 100.0, 0, 0)));
    }

    @Test
    @DisplayName("Testing login users")
    @Order(2)
    void testUserLogin() {
        assertEquals("altenhof-mtcgToken", db.loginUser("altenhof", "passwort") );
        assertEquals("admin-mtcgToken", db.loginUser("admin", "passwort"));
        assertNull(db.loginUser("altenhof", "passwort2"));
        assertNull(db.loginUser("non-existent", "passwort2"));
        assertEquals(db.loginUser("kienboec", "passwort"), "kienboec-mtcgToken");
        assertEquals(db.loginUser("You", "you"), "You-mtcgToken");
    }

    @Test
    @DisplayName("Testing Token checking measures users")
    @Order(3)
    void testUserTokenValidation() throws InterruptedException {
        assertTrue(db.tokenMatchesUsername("altenhof-mtcgToken", "altenhof"));
        assertFalse(db.tokenMatchesUsername("kienboec-mtcgToken", "altenhof"));

        // while testing this function, I reduced the time the token was valid to 5 seconds. Therefore this statement wouldn't trigger a test error.
        //Thread.sleep(5000);
        //assertFalse(db.isTokenValid("altenhof-mtcgToken"));
    }

    @Test
    @DisplayName("Testing update users")
    @Order(4)
    void testUserUpdate() {
        assertTrue(db.updateUser(new UserRecord(null, "passwort2", null, "just cool lecturer things", null, null, null, null, null), "altenhof", "altenhof-mtcgToken"));
        assertFalse(db.updateUser(new UserRecord("kienboec", null, null, null, null, null, null, null, null), "altenhof", "altenhof-mtcgToken"));
    }

    @Test
    @DisplayName("Testing getUserData")
    @Order(5)
    void testUserDataRetrieval() {
        assertEquals(
                new UserRecord("altenhof", db.generateHash("passwort2"), "Markus Altenhofer", "just cool lecturer things", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB", 20, 100.0, 0, 0),
                db.getUserData("altenhof-mtcgToken", "altenhof"));

        assertEquals(
                new UserRecord("kienboec", db.generateHash("passwort"), "Daniel Kienböck", "just chillin'", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB", 20, 100.0, 0, 0),
                db.getUserData("kienboec-mtcgToken", "kienboec"));

        assertNotEquals(
                new UserRecord("kienboec", db.generateHash("falsches Passwort"), "Daniel Kienböck", "just chillin'", "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB", 20, 100.0, 0, 0),
                db.getUserData("kienboec-mtcgToken", "kienboec"));
    }


    /*
    #####
            PACKAGES
    #####
    */

    @Test
    @DisplayName("Testing package insert")
    @Order(6)
    void testPackageInsert() {
        List<CardRecord> cardRecords = new ArrayList<>(Arrays.asList(
                new CardRecord("Dragon1", "FireDragon", 35.0),
                new CardRecord("Lucky1", "FireLucky", 33.0),
                new CardRecord("Spell1", "Spell", 30.0),
                new CardRecord("Elf1", "IceElf", 45.0)));

        // not enough cards
        assertFalse(db.insertPackage(cardRecords, "admin-mtcgToken"));

        cardRecords.add(new CardRecord("Dragon2", "WaterDragon", 55.0));

        // not done by admin
        assertFalse(db.insertPackage(cardRecords, "kienboec-mtcgToken"));
        assertTrue(db.insertPackage(cardRecords, "admin-mtcgToken"));
        // cards with same ids already exist
        assertFalse(db.insertPackage(cardRecords, "admin-mtcgToken"));


        List<CardRecord> cardRecords2 = new ArrayList<>(Arrays.asList(
                new CardRecord("2-Elf1", "FireElf", 30.0),
                new CardRecord("2-Knight1", "Knight", 70.0),
                new CardRecord("2-Spell1", "WaterSpell", 33.0),
                new CardRecord("2-Spell2", "WindSpell", 45.0),
                new CardRecord("2-Goblin1", "Goblin", 50.0)));
        assertTrue(db.insertPackage(cardRecords2, "admin-mtcgToken"));

        List<CardRecord> cardRecords3 = new ArrayList<>(Arrays.asList(
                new CardRecord("3-Ork1", "FireOrk", 40.0),
                new CardRecord("3-Knight1", "FireKnight", 60.0),
                new CardRecord("3-Spell1", "WindSpell", 43.0),
                new CardRecord("3-Kraken1", "WaterKraken", 42.0),
                new CardRecord("3-Goblin1", "Goblin", 25.0)));
        assertTrue(db.insertPackage(cardRecords3, "admin-mtcgToken"));
    }

    @Test
    @DisplayName("Testing buying packages")
    @Order(7)
    void testBuyingPackage() {
        // token invalid
        assertEquals("token", db.buyPackage("someguy-mtcgToken", false));

        assertTrue(db.buyPackage("admin-mtcgToken", false).startsWith("### Ranking"));

        // admin only starts with 5 coins in prior test case
        assertEquals("coins", db.buyPackage("admin-mtcgToken", false));

        assertTrue(db.buyPackage("altenhof-mtcgToken", false).startsWith("### Ranking"));
        assertTrue(db.buyPackage("altenhof-mtcgToken", false).startsWith("### Ranking"));

        // only three packages added in previous test
        assertEquals("package", db.buyPackage("altenhof-mtcgToken", false));
    }

    @Test
    @DisplayName("Testing card aquiring")
    @Order(8)
    void testGettingCards () {
        List<Card> cards = new ArrayList<>(Arrays.asList(
                new Dragon(35.0, Element.FIRE, "Dragon1"),
                new Lucky(33.0, Element.FIRE, "Lucky1"),
                new Spell(30.0, Element.NORMAL, "Spell1"),
                new Elf(45.0, Element.ICE, "Elf1"),
                new Dragon(55.0, Element.WATER, "Dragon2")));

        List<Card> actualCards = db.getCards("admin-mtcgToken", false);
        assertTrue(areCardsTheSame(actualCards, cards));

        assertNull(db.getCards("someGuy", false));

        assertEquals(db.getCards("You-mtcgToken", false).size(), 0);
        assertEquals(db.getCards("altenhof-mtcgToken", true).size(), 0);
    }


    /*
    #####
            DECK
    #####
    */


    @Test
    @DisplayName("Testing updating deck")
    @Order(9)
    void testUpdatingDeck () {
        List<String> cardIdsAdmin = new ArrayList<>(Arrays.asList(
                "Dragon1",
                "Lucky1",
                "Elf1",
                "Dragon2"
        ));
        List<String> cardIdsAltenhof = new ArrayList<>(Arrays.asList(
                "2-Knight1",
                "2-Spell1",
                "2-Spell2"
        ));

        // non-existent token
        assertFalse(db.updateDeck("someguy", cardIdsAdmin));

        assertTrue(db.updateDeck("admin-mtcgToken", cardIdsAdmin));

        // altenhof does not own these cards
        assertFalse(db.updateDeck("altenhof-mtcgToken", cardIdsAdmin));

        // not enough cards
        assertFalse(db.updateDeck("altenhof-mtcgToken", cardIdsAltenhof));

        cardIdsAltenhof.add("2-Goblin1");

        // admin does not own these cards
        assertFalse(db.updateDeck("admin-mtcgToken", cardIdsAltenhof));
        // admin should still have his previous deck
        List<String> adminCards = db.getCards("admin-mtcgToken", true).stream().map(Card::getId).collect(Collectors.toList());
        // order is unimportant
        assertTrue(areCardIdsTheSame(adminCards, cardIdsAdmin));

        List<String> cardIdsAdmin2 = new ArrayList<>(Arrays.asList(
                "Lucky1",
                "Spell1",
                "Elf1",
                "Dragon2"
        ));

        // admin now wants to replace his first dragon-card with a spell card, that he possesses
        assertTrue(db.updateDeck("admin-mtcgToken", cardIdsAdmin2));
        adminCards = db.getCards("admin-mtcgToken", true).stream().map(Card::getId).collect(Collectors.toList());
        assertTrue(areCardIdsTheSame(adminCards, cardIdsAdmin2));


        // now admin wants to alter his deck and replace his spell with a card, that is not in his possession
        cardIdsAdmin2.set(1, "2-Knight1");
        assertFalse(db.updateDeck("admin-mtcgToken", cardIdsAdmin2));

        cardIdsAdmin2.set(1, "Spell1");
        adminCards = db.getCards("admin-mtcgToken", true).stream().map(Card::getId).collect(Collectors.toList());
        assertTrue(areCardIdsTheSame(adminCards, cardIdsAdmin2));

        assertTrue(db.updateDeck("altenhof-mtcgToken", cardIdsAltenhof));
    }


    /*
    #####
            STATS
    #####
    */

    @Test
    @DisplayName("Testing showing stats")
    @Order(10)
    void testShowingStats () {
        assertNull(db.getStats("someguy"));

        assertEquals(db.getStats("altenhof-mtcgToken"), String.format("Elo: %.1f |  Games: %d | Wins: %d%n", 100.0, 0, 0));
        assertEquals(db.getStats("admin-mtcgToken"), String.format("Elo: %.1f |  Games: %d | Wins: %d%n", 100.0, 0, 0));
    }

    /*
    #####
            BATTLE
    #####
    */

    @Test
    @DisplayName("Test holding a battle")
    @Order(11)
    void testSimulationOfBattle () {
        assertNull(db.simulateBattle("someguy", "yoyo"));

        assertTrue(db.simulateBattle("admin-mtcgToken", "altenhof-mtcgToken").startsWith("Battle begins: Starting attacker: "));
        UserRecord admin = db.getUserData("admin-mtcgToken", "admin");
        UserRecord altenhof = db.getUserData("altenhof-mtcgToken", "altenhof");
        assertTrue(admin.Elo() == 103.0 || admin.Elo() == 97.0);
        assertTrue(altenhof.Elo() == 103.0 || altenhof.Elo() == 97.0);
    }


    /*
    #####
            TRADING
    #####
    */

    @Test
    @DisplayName("testing adding trade offers and listing them")
    @Order(12)
    void testTradeCRUD() {
        TradeOfferRecord tradeOfferRecord = new TradeOfferRecord("Trade1", "Wizard1", "monster", 50.0);
        // doesn't own card
        assertFalse(db.insertTradeOffer("someguy", tradeOfferRecord));

        // card is in deck
        assertFalse(db.insertTradeOffer("admin-mtcgToken", tradeOfferRecord));

        tradeOfferRecord = new TradeOfferRecord("Trade1", "Dragon1", "monster", 50.0);
        assertTrue(db.insertTradeOffer("admin-mtcgToken", tradeOfferRecord));

        assertNull(db.getTradingOffers("someguy"));
        assertEquals(db.getTradingOffers("altenhof-mtcgToken").size(), 1);

        // test whether card is locked
        List<String> adminCards = db.getCards("admin-mtcgToken", false).stream().filter(Card::isLockedFromUsing).map(Card::getId).collect(Collectors.toList());
        assertTrue(adminCards.size() == 1 && adminCards.get(0).equals("Dragon1"));
        // test if it can be added to deck
        List<String> cardIdsAdmin = new ArrayList<>(Arrays.asList(
                "Wizard1",
                "Dragon1",
                "Elf1",
                "Dragon2"
        ));
        assertFalse(db.updateDeck("admin-mtcgToken", cardIdsAdmin));

        // test if you can make trade offer with card, that is currently in deck
        assertFalse(db.insertTradeOffer("admin-mtcgToken", new TradeOfferRecord("Trade2", "Spell1", "spell", 20.0)));


        // it's not altenhof's trade offer
        assertFalse(db.deleteTradingOffer("altenhof-mtcgToken", "Trade1"));
        assertTrue(db.deleteTradingOffer("admin-mtcgToken", "Trade1"));

        // check if card is back in deck
        adminCards = db.getCards("admin-mtcgToken", false).stream().filter(x -> !x.isLockedFromUsing()).map(Card::getId).collect(Collectors.toList());
        assertTrue(adminCards.contains("Dragon1"));

        // add card back to trading area
        assertTrue(db.insertTradeOffer("admin-mtcgToken", tradeOfferRecord));

        // You can't trade with yourself and card is locked
        assertFalse(db.tryToTrade("admin-mtcgToken", "Trade1", "Spell1"));

        // card in deck
        assertFalse(db.tryToTrade("altenhof-mtcgToken", "Trade1", "2-Goblin1"));

        // requirements not met - too weak
        assertFalse(db.tryToTrade("altenhof-mtcgToken", "Trade1", "2-Elf1"));
        // not a monster
        assertFalse(db.tryToTrade("altenhof-mtcgToken", "Trade1", "2-Spell1"));

        assertTrue(db.tryToTrade("altenhof-mtcgToken", "Trade1", "3-Knight1"));
        assertEquals(db.getTradingOffers("altenhof-mtcgToken").size(), 0);

        // admin shouldn't have the Dragon1 card anymore and altenhof should have received the Dragon1 card
        adminCards = db.getCards("admin-mtcgToken", false).stream().map(Card::getId).collect(Collectors.toList());
        List<String> altenhofCards = db.getCards("altenhof-mtcgToken", false).stream().map(Card::getId).collect(Collectors.toList());
        assertFalse(adminCards.contains("Dragon1"));
        assertTrue(altenhofCards.contains("Dragon1"));

    }

}
