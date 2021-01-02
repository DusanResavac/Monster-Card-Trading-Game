package mctg.traps;


import mctg.Card;
import mctg.Element;
import mctg.Monster;
import mctg.Trap;

/**
 * This kind of trap card is evil towards rich money bags, because it nullifies damage from rare, strong monsters.
 * Something to keep in mind is that the lego itself often comes with a relatively low amount of damage, so knowledge about
 * your opponent can help you determine whether it's a good pick for your deck
 */
public class Lego extends Trap {

    public Lego(double damage, Element element, String id) {
        super(damage, element, id);
    }

    @Override
    public double calculateIncomingDamage(Card card) {
        try {
            // zb mctg.monsters.Kraken
            if (Monster.class.isAssignableFrom(Class.forName(card.getClass().getCanonicalName())) &&
                    card.getDamage() >= 50) {
                return 0;
            }
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            System.err.println("Error in retrieving class name - LegoClass");
            return super.calculateIncomingDamage(card);
        }

        return super.calculateIncomingDamage(card);
    }
}
