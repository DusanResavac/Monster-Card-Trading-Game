public class SpellCard extends Card implements DamageCalculation {

    public SpellCard(double damage, Element element) {
        super(damage, element);
    }


    public double calculateIncomingDamage(Card card) {
        return card.getDamage() * DamageMultiplier.getElementMultiplier(card.getElement(), getElement());
    }
}
