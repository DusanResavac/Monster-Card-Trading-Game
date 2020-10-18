
public class Dragon extends MonsterCard {

    public Dragon(double damage, Element element) {
        super(damage, element);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        if (card instanceof Goblin) {
            return 0;
        }

        return super.calculateIncomingDamage(card);
    }
}
