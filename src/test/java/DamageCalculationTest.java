import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DamageCalculationTest {

    SpellCard scWater;
    Goblin goblin;
    Knight knight;
    Dragon dragon;
    FireElf fireElf;
    Wizard wizard;
    Ork ork;


    @BeforeEach
    static void beforeEach() {
        scWater = new SpellCard (60, Element.WATER);
        goblin = new Goblin (75, Element.NORMAL);
        knight = new Knight (100, Element.NORMAL);
        dragon = new Dragon (80, Element.WATER);
        fireElf = new FireElf (65, Element.FIRE);
        wizard = new Wizard (70, Element.FIRE);
        ork = new Ork (85, Element.NORMAL);
    }

    @Test
    @DisplayName("Calculating Damage | Def: various MonsterCards, Atk: various MonsterCards")
    void calcIncomingDamage_mc_mc() {
        // Goblin (x, y) against Dragon (a, b) -> Dragon should win (Goblin deals 0 Damage)
        assertTrue("Dragon should deal more Damage (Goblin to scared)", goblin.calculateIncomingDamage(dragon) > dragon.calculateIncomingDamage(goblin));

        // Goblin against Wizard -> Goblin should win (Elements are ignored)
        assertTrue("Wizard should receive more damage", goblin.calculateIncomingDamage(wizard) < wizard.calculateIncomingDamage(goblin));

        // Dragon against FireElf -> FireElf should win (FireElf dodges attacks)
        assertTrue("Dragon should receive more damage", dragon.calculateIncomingDamage(fireElf) > fireElf.calculateIncomingDamage(dragon));
    }

    @Test
    @DisplayName("Calculating Damage | Def: Spellcard, Atk: Goblin, Dragon, FireElf, Knight, Spellcard(Fire)")
    void calcIncomingDamage_sc_mcs () {
        Kraken kraken = new Kraken (50, Element.WATER);

        // Spellcard (60, Water) against Dragon (80, Water) -> Dragon should Win
        assertTrue("SpellCardWater should receive more Damage than the dragon", scWater.calculateIncomingDamage(dragon) > dragon.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against Knight (100, Normal) -> SpellCard should Win
        assertTrue("Knight should receive max possible damage (drowns)", scWater.calculateIncomingDamage(knight) < knight.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against Goblin (75, Normal) -> Goblin should Win
        assertTrue("SpellCardWater should receive more Damage than the Goblin", scWater.calculateIncomingDamage(goblin) > goblin.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against FireElf (65, Fire) -> SpellCard should Win
        assertTrue("FireElf should receive more Damage than the SpellCardWater", scWater.calculateIncomingDamage(fireElf) < fireElf.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against Kraken (50, Water) -> Kraken should Win
        assertTrue("SpellCardWater should receive more Damage than the kraken", scWater.calculateIncomingDamage(kraken) < kraken.calculateIncomingDamage(scWater));
    }

    @Test
    @DisplayName("Calculating Damage | Def: various Spellcards, Atk: various Spellcards")
    void calcIncomingDamage_sc_sc() {
        SpellCard scFire = new SpellCard (70, Element.FIRE);
        SpellCard scNormal = new SpellCard (40, Element.Normal);
        SpellCard scWater2 = new SpellCard (80, Element.Water);

        // (60, Water) against (70, Fire) -> (60, Water) should Win
        assertTrue("SpellCardFire should receive more Damage than the water spell", scWater.calculateIncomingDamage(scFire) < scFire.calculateIncomingDamage(scWater));

        // (40, Normal) against (70, Fire) -> (70, Fire) should Win
        assertTrue("SpellCardNormal should receive more damage than the fire spell", scNormal.calculateIncomingDamage(scFire) > scFire.calculateIncomingDamage(scNormal));

        // (40, Normal) against (80, Water) -> No Winner (attacker priority should be implemented by round System)
        assertTrue("Both should deal damage the same amount of damage", scNormal.calculateIncomingDamage(scWater2) == scWater2.calculateIncomingDamage(scNormal));

        // (60, Water) against (80, Water) -> (80, Water)
        assertTrue("(60, Water) should receive more damage (80, Water)", scWater.calculateIncomingDamage(scWater2) > scWater2.calculateIncomingDamage(scNormal));
    }


}
