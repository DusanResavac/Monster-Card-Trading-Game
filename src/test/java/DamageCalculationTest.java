import mctg.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DamageCalculationTest {

    private static Spell scWater;
    private static Goblin goblin;
    private static Knight knight;
    private static Dragon dragon;
    private static Elf elf;
    private static Wizard wizard;
    private static Ork ork;

    /*
    @BeforeAll
    static void beforeAll() {
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
        // mctg.Goblin (x, y) against mctg.Dragon (a, b) -> mctg.Dragon should win (mctg.Goblin deals 0 Damage)
        //"mctg.Dragon should deal more Damage (mctg.Goblin to scared)"
        assertTrue(goblin.calculateIncomingDamage(dragon) > dragon.calculateIncomingDamage(goblin));

        // mctg.Goblin against mctg.Wizard -> mctg.Goblin should win (Elements are ignored)
        //"mctg.Wizard should receive more damage"
        assertTrue(goblin.calculateIncomingDamage(wizard) < wizard.calculateIncomingDamage(goblin));

        // mctg.Dragon against mctg.FireElf -> mctg.FireElf should win (mctg.FireElf dodges attacks)
        //"mctg.Dragon should receive more damage"
        assertTrue(dragon.calculateIncomingDamage(fireElf) > fireElf.calculateIncomingDamage(dragon));
    }

    @Test
    @DisplayName("Calculating Damage | Def: Spellcard, Atk: mctg.Goblin, mctg.Dragon, mctg.FireElf, mctg.Knight, Spellcard(Fire)")
    void calcIncomingDamage_sc_mcs () {
        Kraken kraken = new Kraken (50, Element.WATER);

        // Spellcard (60, Water) against mctg.Dragon (80, Water) -> mctg.Dragon should Win
        //"SpellCardWater should receive more Damage than the dragon"
        assertTrue(scWater.calculateIncomingDamage(dragon) > dragon.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against mctg.Knight (100, Normal) -> mctg.SpellCard should Win
        //"mctg.Knight should receive max possible damage (drowns)"
        assertTrue(scWater.calculateIncomingDamage(knight) < knight.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against mctg.Goblin (75, Normal) -> mctg.Goblin should Win
        //"SpellCardWater should receive more Damage than the mctg.Goblin"
        assertTrue(scWater.calculateIncomingDamage(goblin) > goblin.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against mctg.FireElf (65, Fire) -> mctg.SpellCard should Win
        //"mctg.FireElf should receive more Damage than the SpellCardWater"
        assertTrue(scWater.calculateIncomingDamage(fireElf) < fireElf.calculateIncomingDamage(scWater));

        // Spellcard (60, Water) against mctg.Kraken (50, Water) -> mctg.Kraken should Win
        //"SpellCardWater should receive more Damage than the kraken"
        assertTrue(scWater.calculateIncomingDamage(kraken) > kraken.calculateIncomingDamage(scWater));
    }

    @Test
    @DisplayName("Calculating Damage | Def: various Spellcards, Atk: various Spellcards")
    void calcIncomingDamage_sc_sc() {
        SpellCard scFire = new SpellCard (70, Element.FIRE);
        SpellCard scNormal = new SpellCard(40, Element.NORMAL);
        SpellCard scWater2 = new SpellCard (160, Element.WATER);

        // (160, Water) against (70, Fire) -> (160, Water) should Win
        // "SpellCardFire should receive more Damage than the water spell"
        assertTrue(scWater.calculateIncomingDamage(scFire) < scFire.calculateIncomingDamage(scWater));

        // (40, Normal) against (70, Fire) -> (70, Fire) should Win
        // "SpellCardNormal should receive more damage than the fire spell"
        assertTrue(scNormal.calculateIncomingDamage(scFire) > scFire.calculateIncomingDamage(scNormal));

        // (40, Normal) against (160, Water) -> No Winner (attacker priority should be implemented by round System)
        //"Both should deal damage the same amount of damage"
        assertEquals(scWater2.calculateIncomingDamage(scNormal), scNormal.calculateIncomingDamage(scWater2));

        // (160, Water) against (60, Water) -> (160, Water)
        //"(60, Water) should receive more damage against (160, Water)"
        assertTrue(scWater.calculateIncomingDamage(scWater2) > scWater2.calculateIncomingDamage(scWater));
    }

     */


}
