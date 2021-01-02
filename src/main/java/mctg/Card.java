package mctg;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public abstract class Card {
    @Getter private final double damage;
    @Getter private final Element element;
    @Getter private final String id;
    @Getter @Setter private boolean lockedFromUsing = false;
    @Getter @Setter private boolean inDeck = false;

    public static final HashMap<String, int[]> rarityDamage = new HashMap<>(){{
        put("Goblin", new int[]{8, 14, 20, 30, 40});
        put("Ork", new int[]{25, 35, 40, 45, 55});
        put("Elf", new int[]{10, 15, 25, 35, 40});
        put("Dragon", new int[]{30, 40, 45, 50, 60});
        put("Knight", new int[]{20, 30, 40, 50, 60});
        put("Spell", new int[]{15, 25, 35, 45, 55});
        put("Wizard", new int[]{20, 30, 40, 50, 55});
        put("Kraken", new int[]{15, 25, 33, 46, 62});
    }};

    public Card (double damage, Element element, String id) {
        this.damage = damage;
        this.element = element;
        this.id = id;
    }

    public abstract double calculateIncomingDamage(Card card);

    public boolean equals (Card card) {
        return card.getClass() == this.getClass() && card.getElement() == getElement() && card.getDamage() == this.getDamage();
    }

    public String toStringSimple() {
        return String.format("%s-%s-%.1f", this.getClass().getSimpleName(), element.toString(), damage);
    }

    public String toStringShort () {
        return String.format("%8s-%-6s-%4.1f", this.getClass().getSimpleName(), element.toString(), damage);
    }

    public String toStringPlain () {
        return String.format("%-8s - Damage: %4.1f - Element: %-6s", this.getClass().getSimpleName(), damage, element.toString());
    }

    public String toStringTerminal () {
        StringBuilder stars = new StringBuilder();
        for (int boundary: rarityDamage.get(this.getClass().getSimpleName())) {
            if (damage >= boundary) {
                stars.append("*");
            }
        }
        stars.append("*");
        return String.format(" %-8s - Damage: %5.1f - Element: %-6s - InDeck: %5b - Locked: %5b - Stars: %s", this.getClass().getSimpleName(), damage, element.toString(), inDeck, lockedFromUsing, stars.toString());
    }

    public String toString () {
        StringBuilder stars = new StringBuilder();
        for (int boundary: rarityDamage.get(this.getClass().getSimpleName())) {
            if (damage >= boundary) {
                stars.append("\uD83D\uDFCA");
            }
        }
        stars.append("\uD83D\uDFCA");
        return String.format(" %-8s - Damage: %5.1f - Element: %-6s - InDeck: %5b - Locked: %5b - Stars: %s", this.getClass().getSimpleName(), damage, element.toString(), inDeck, lockedFromUsing, stars.toString());
    }


}
