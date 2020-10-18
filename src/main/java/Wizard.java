public class Wizard extends MonsterCard {

    public Wizard(double damage, Element element) {
        super(damage, element);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof Ork) {
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
