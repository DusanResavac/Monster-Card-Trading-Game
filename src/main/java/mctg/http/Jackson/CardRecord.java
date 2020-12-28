package mctg.http.Jackson;

import java.io.Serializable;

public record CardRecord(
    String Id,
    String Name,
    Double Damage
) implements Serializable {}
