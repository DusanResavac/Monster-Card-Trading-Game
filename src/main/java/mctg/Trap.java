package mctg;

public abstract class Trap extends Card {

    public Trap(double damage, Element element, String id) {
        super(damage, element, id);
    }

    public double calculateIncomingDamage(Card card) {
        return card.getDamage() * DamageMultiplier.getElementMultiplier(card.getElement(), getElement());
    }
}
