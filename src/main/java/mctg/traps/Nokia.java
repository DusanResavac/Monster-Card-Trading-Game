package mctg.traps;

import mctg.Card;
import mctg.Element;
import mctg.Trap;

/**
 * This is usually for someone who believes in the heart of cards, because it doesn't deal damage, but also doesn't receive it
 * Since the attacker at the beginning and the cards are randomly chosen, and the attacker alternates each round, in case this
 * card is played, the attacker of that round is going to win it.
 */
public class Nokia extends Trap {
    public Nokia(double damage, Element element, String id) {
        super(0.0, element, id);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        // unable to receive damage
        return 0;
    }
}
