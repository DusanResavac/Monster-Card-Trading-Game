import lombok.Getter;
import lombok.Setter;

public abstract class Card {
    @Getter private final double damage;
    @Getter private final Element element;
    @Getter @Setter private boolean lockedFromUsing = false;

    public Card (double damage, Element element) {
        this.damage = damage;
        this.element = element;
    }

    public boolean equals (Card card) {
        return card.getClass() == this.getClass() && card.getElement() == getElement() && card.getDamage() == this.getDamage();
    }
}
