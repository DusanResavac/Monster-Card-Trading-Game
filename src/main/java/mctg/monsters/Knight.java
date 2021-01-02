package mctg.monsters;

import mctg.Card;
import mctg.Element;
import mctg.Monster;
import mctg.Spell;

public class Knight extends Monster {

    public Knight(double damage, Element element, String id) {
        super(damage, element, id);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof Spell && card.getElement() == Element.WATER) {
            // vielleicht overkill .. naja mal schauen
            return Double.MAX_VALUE;
        }

        return super.calculateIncomingDamage(card);
    }
}
