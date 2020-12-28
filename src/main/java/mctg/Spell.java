package mctg;

public class Spell extends Card implements DamageCalculation {

    public Spell(double damage, Element element, String id) {
        super(damage, element, id);
    }


    public double calculateIncomingDamage(Card card) {
        return card.getDamage() * DamageMultiplier.getElementMultiplier(card.getElement(), getElement());
    }
}
