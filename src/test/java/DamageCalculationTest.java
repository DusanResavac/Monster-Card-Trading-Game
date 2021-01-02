import mctg.*;
import mctg.monsters.*;
import mctg.traps.Lego;
import mctg.traps.Lucky;
import mctg.traps.Nokia;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    
    @BeforeAll
    static void beforeAll() {
        scWater = new Spell (60, Element.WATER, "Card1");
        goblin = new Goblin (75, Element.NORMAL, "Card2");
        knight = new Knight (100, Element.NORMAL, "Card3");
        dragon = new Dragon(80, Element.WATER, "Card4");
        elf = new Elf (65, Element.FIRE, "Card5");
        wizard = new Wizard(70, Element.FIRE, "Card6");
        ork = new Ork(85, Element.NORMAL, "Card7");
    }

    @Test
    @DisplayName("Calculating Damage | Def: various MonsterCards, Atk: various MonsterCards")
    void calcIncomingDamage_mc_mc() {
        // mctg.monsters.Goblin (x, y) against mctg.monsters.Dragon (a, b) -> mctg.monsters.Dragon should win (mctg.monsters.Goblin deals 0 Damage)
        //"mctg.monsters.Dragon should deal more Damage (mctg.monsters.Goblin to scared)"
        assertTrue(goblin.calculateIncomingDamage(dragon) > dragon.calculateIncomingDamage(goblin));

        // mctg.monsters.Goblin against mctg.monsters.Wizard -> mctg.monsters.Goblin should win (Elements are ignored)
        //"mctg.monsters.Wizard should receive more damage"
        assertTrue(goblin.calculateIncomingDamage(wizard) < wizard.calculateIncomingDamage(goblin));

        // mctg.monsters.Dragon against mctg.FireElf -> mctg.FireElf should win (mctg.FireElf dodges attacks)
        //"mctg.monsters.Dragon should receive more damage"
        assertTrue(dragon.calculateIncomingDamage(elf) > elf.calculateIncomingDamage(dragon));
    }

    @Test
    @DisplayName("Calculating Damage | Def: Spell, Atk: mctg.monsters.Goblin, mctg.monsters.Dragon, mctg.FireElf, mctg.monsters.Knight, Spell(Fire)")
    void calcIncomingDamage_sc_mcs () {
        Kraken kraken = new Kraken (50, Element.WATER, "Card8");

        // Spell (60, Water) against mctg.monsters.Dragon (80, Water) -> mctg.monsters.Dragon should Win
        //"SpellWater should receive more Damage than the dragon"
        assertTrue(scWater.calculateIncomingDamage(dragon) > dragon.calculateIncomingDamage(scWater));

        // Spell (60, Water) against mctg.monsters.Knight (100, Normal) -> mctg.Spell should Win
        //"mctg.monsters.Knight should receive max possible damage (drowns)"
        assertTrue(scWater.calculateIncomingDamage(knight) < knight.calculateIncomingDamage(scWater));

        // Spell (60, Water) against mctg.monsters.Goblin (75, Normal) -> mctg.monsters.Goblin should Win
        //"SpellWater should receive more Damage than the mctg.monsters.Goblin"
        assertTrue(scWater.calculateIncomingDamage(goblin) > goblin.calculateIncomingDamage(scWater));

        // Spell (60, Water) against mctg.FireElf (65, Fire) -> mctg.Spell should Win
        //"mctg.FireElf should receive more Damage than the SpellWater"
        assertTrue(scWater.calculateIncomingDamage(elf) < elf.calculateIncomingDamage(scWater));

        // Spell (60, Water) against mctg.monsters.Kraken (50, Water) -> mctg.monsters.Kraken should Win
        //"SpellWater should receive more Damage than the kraken"
        assertTrue(scWater.calculateIncomingDamage(kraken) > kraken.calculateIncomingDamage(scWater));
    }

    @Test
    @DisplayName("Calculating Damage | Def: various Spells, Atk: various Spells")
    void calcIncomingDamage_sc_sc() {
        Spell scFire = new Spell (70, Element.FIRE, "Card9");
        Spell scNormal = new Spell(40, Element.NORMAL, "Card10");
        Spell scWater2 = new Spell (160, Element.WATER, "Card11");

        // (160, Water) against (70, Fire) -> (160, Water) should Win
        // "SpellFire should receive more Damage than the water spell"
        assertTrue(scWater.calculateIncomingDamage(scFire) < scFire.calculateIncomingDamage(scWater));

        // (40, Normal) against (70, Fire) -> (70, Fire) should Win
        // "SpellNormal should receive more damage than the fire spell"
        assertTrue(scNormal.calculateIncomingDamage(scFire) > scFire.calculateIncomingDamage(scNormal));

        // (40, Normal) against (160, Water) -> No Winner (attacker priority should be implemented by round System)
        //"Both should deal damage the same amount of damage"
        assertEquals(scWater2.calculateIncomingDamage(scNormal), scNormal.calculateIncomingDamage(scWater2));

        // (160, Water) against (60, Water) -> (160, Water)
        //"(60, Water) should receive more damage against (160, Water)"
        assertTrue(scWater.calculateIncomingDamage(scWater2) > scWater2.calculateIncomingDamage(scWater));
    }

    @Test
    @DisplayName("Calculating Damage | Trap cards")
    void calcIncomingDamage_trapmix() {
        Trap nokia = new Nokia(15.0, Element.FIRE, "NOKIA");
        // doesn't matter which element, because of lucky's effect
        Trap lucky = new Lucky(33, Element.ICE, "LUCKY");
        Trap lego = new Lego(30, Element.WIND, "LEGO");

        Dragon tDragon1 = new Dragon(55, Element.WATER, "DRAGON");
        Dragon tDragon2 = new Dragon(55, Element.FIRE, "DRAGON");
        Goblin tGoblin1 = new Goblin(40, Element.NORMAL, "GOBLIN");

        assertEquals(nokia.calculateIncomingDamage(tDragon1), tDragon1.calculateIncomingDamage(nokia));
        assertEquals(nokia.calculateIncomingDamage(knight), knight.calculateIncomingDamage(nokia));

        // Goblin damage < 50 -> normal fight between trap and monster.
        // Goblin should win, because 30 * 1,1 < 40 * 0,9090909
        assertTrue(lego.calculateIncomingDamage(tGoblin1) > tGoblin1.calculateIncomingDamage(lego));
        // lego effect is being activated, because the dragon's damage is >= 50 and thus lego doesn't receive damage
        assertTrue(lego.calculateIncomingDamage(tDragon2) < tDragon2.calculateIncomingDamage(lego));

        for (int i = 0; i < 10; i++) {
            double result = tDragon1.calculateIncomingDamage(lucky);
            // check if effect was activated
            if (result == 33 * 2.5) {
                assertTrue(lucky.calculateIncomingDamage(tDragon1) < result);
            } else {
                assertTrue(lucky.calculateIncomingDamage(tDragon1) > result);
            }
        }

        Wizard tWizard = new Wizard(30, Element.ICE, "WIZARD");
        // ICE deals 3 x damage to water -> 90
        // Water deals 1/3 x damage to ice -> 20
        assertEquals(scWater.calculateIncomingDamage(tWizard), 90);
        assertEquals(tWizard.calculateIncomingDamage(scWater), 20);

        assertEquals(tWizard.calculateIncomingDamage(lego), lego.calculateIncomingDamage(tWizard));
    }


}
