package mctg;

public class Elf extends MonsterCard {

    public Elf(double damage, Element element, String id) {
        super(damage, element, id);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof Dragon) {
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
