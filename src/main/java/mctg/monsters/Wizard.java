package mctg.monsters;

import mctg.Card;
import mctg.Element;
import mctg.Monster;

public class Wizard extends Monster {

    public Wizard(double damage, Element element, String id) {
        super(damage, element, id);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof Ork) {
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
