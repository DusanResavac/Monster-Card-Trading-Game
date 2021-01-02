package mctg;

import mctg.traps.Lucky;

public abstract class Monster extends Card implements DamageCalculation {

    public Monster(double damage, Element element, String id) {
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

        // consider Element types to calculate damage if the other card is a Spell or Trap
        if (card instanceof Spell || card instanceof Trap) {
            return card.getDamage() * DamageMultiplier.getElementMultiplier(card.getElement(), getElement());
        }

        // if it's an other card just take the damage
        return card.getDamage();
    }
}
