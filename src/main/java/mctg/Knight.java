package mctg;

public class Knight extends MonsterCard {

    public Knight(double damage, Element element) {
        super(damage, element);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof SpellCard && card.getElement() == Element.WATER) {
            // vielleicht overkill .. naja mal schauen
            return Double.MAX_VALUE;
        }

        return super.calculateIncomingDamage(card);
    }
}
