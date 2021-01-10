package mctg;

import mctg.traps.Lucky;

public abstract class Trap extends Card implements DamageCalculation {

    public Trap(double damage, Element element, String id) {
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
