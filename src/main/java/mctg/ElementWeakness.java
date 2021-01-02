package mctg;

import lombok.Getter;

public class ElementWeakness {
    @Getter
    private Element attacker;
    @Getter
    private Element defender;
    @Getter
    private double multiplier;

    public ElementWeakness (Element attacker, Element defender, double multiplier) {
        this.attacker = attacker;
        this.defender = defender;
        this.multiplier = multiplier;
    }
}
