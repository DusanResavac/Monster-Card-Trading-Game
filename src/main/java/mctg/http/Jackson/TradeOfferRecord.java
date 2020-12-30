package mctg.http.Jackson;

import java.io.Serializable;

public record TradeOfferRecord(
        String Id,
        String CardToTrade,
        String Type,
        Double MinimumDamage
) implements Serializable {}
