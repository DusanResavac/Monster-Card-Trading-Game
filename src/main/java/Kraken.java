public class Kraken extends MonsterCard {

    public Kraken(double damage, Element element) {
        super(damage, element);
    }

    public double calculateIncomingDamage(Card card) {
        if (card instanceof SpellCard) {
            // impudent >:(
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
