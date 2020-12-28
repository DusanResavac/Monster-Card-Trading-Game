package mctg;

public class Kraken extends MonsterCard {

    public Kraken(double damage, Element element, String id) {
        super(damage, element, id);
    }

    public double calculateIncomingDamage(Card card) {
        if (card instanceof Spell) {
            // impudent >:(
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
