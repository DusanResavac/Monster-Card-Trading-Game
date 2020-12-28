package mctg;

public class Knight extends MonsterCard {

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
