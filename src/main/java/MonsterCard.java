public abstract class MonsterCard extends Card implements DamageCalculation {

    public static void main(String[] args) {
        Kraken kraken = new Kraken(30, Element.WATER);
        Ork ork = new Ork(20, Element.NORMAL);
        Kraken kraken2 = new Kraken(40, Element.WATER);
        Kraken kraken3 = new Kraken(30, Element.WATER);
        SpellCard sc = new SpellCard(60, Element.NORMAL);
        SpellCard sc2 = new SpellCard(60, Element.FIRE);
        SpellCard sc3 = new SpellCard(60, Element.NORMAL);


        System.out.println(ork.calculateIncomingDamage(sc2));
    }

    public MonsterCard(double damage, Element element) {
        super(damage, element);
    }

    public double calculateIncomingDamage(Card card) {
        // consider Element types to calculate damage if the other card is a SpellCard
        if (card instanceof SpellCard) {
            return card.getDamage() * DamageMultiplier.getElementMultiplier(card.getElement(), getElement());
        }

        // if it's a monstercard just take the damage
        return card.getDamage();
    }
}
