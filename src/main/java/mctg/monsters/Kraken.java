package mctg.monsters;

import mctg.Card;
import mctg.Element;
import mctg.Monster;
import mctg.Spell;

public class Kraken extends Monster {

    public Kraken(double damage, Element element, String id) {
        super(damage, element, id);
    }

    public double calculateIncomingDamage(Card card) {
        if (card instanceof Spell) {
            // impudent >:(
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
