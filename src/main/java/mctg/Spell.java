package mctg;

import mctg.traps.Lucky;

public class Spell extends Card implements DamageCalculation {

    public Spell(double damage, Element element, String id) {
        super(damage, element, id);
    }


    public double calculateIncomingDamage(Card card) {
        if (card instanceof Lucky) {
            if (Math.random() >= 0.7) {
                return card.getDamage() * 2.5;
            } else {
                return card.getDamage();
            }
        }

        return card.getDamage() * DamageMultiplier.getElementMultiplier(card.getElement(), getElement());
    }
}
