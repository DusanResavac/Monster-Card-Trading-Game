public class FireElf extends MonsterCard {

    public FireElf(double damage, Element element) {
        super(damage, element);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof Dragon) {
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
