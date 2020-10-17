import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class CardTest {
    @Test
    @DisplayName("Cards equal | SpellCard, Kraken")
    public void testEquals_SC_Kraken () {
        Card sc = new SpellCard (50, Element.FIRE);
        MonsterCard kraken = new Kraken (40, Element.WATER);

        assertFalse("SpellCard shouldn't be equal to Kraken", sc.equals(kraken));
    }

    @Test
    @DisplayName("Cards equal | SpellCard, Wizard")
    public void testEquals_SC_Wizard () {
        Card sc = new SpellCard (50, Element.FIRE);
        MonsterCard wizard = new Wizard (40, Element.NORMAL);

        assertFalse("SpellCard shouldn't be equal to Wizard", sc.equals(wizard));
    }

    @Test
    @DisplayName("Cards equal | Kraken, Wizard")
    public void testEquals_Kraken_Wizard () {
        MonsterCard kraken = new Kraken (50, Element.WATER);
        MonsterCard wizard = new Wizard (70, Element.NORMAL);

        assertFalse("Kraken shouldn't be equal to Wizard", kraken.equals(wizard));
    }

    @Test
    @DisplayName("Cards equal | Dragon-Fire, Dragon-Water")
    public void testEquals_Dragon_Dragon () {
        MonsterCard dragon = new Dragon (80, Element.FIRE);
        MonsterCard dragon2 = new Dragon (65, Element.WATER);

        assertFals("Dragon is not equal to Dragon with different stats", dragon.equals(dragon2));
    }

}
