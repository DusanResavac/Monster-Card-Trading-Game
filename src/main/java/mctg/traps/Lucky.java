package mctg.traps;

import mctg.Card;
import mctg.Element;
import mctg.Trap;

/**
 * Like taking the high roll? Then this card might be for you! With a chance of 30%, you might 250% of the damage to the enemy card.
 * The damage multiplier is disabled, when a Lucky card attacking/defending
 */
public class Lucky extends Trap {

    public Lucky(double damage, Element element, String id) {
        super(damage, element, id);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        return card.getDamage();
    }
}
