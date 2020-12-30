package mctg;

import lombok.Getter;

public class TradeOffer {
    @Getter
    private Card card;
    @Getter
    private String id;
    @Getter
    private double minimumDamage;
    @Getter
    private String type;

    public TradeOffer (Card card, String id, double minimumDamage, String type) {
        this.card = card;
        this.id = id;
        this.minimumDamage = minimumDamage;
        this.type = type;
    }
}
