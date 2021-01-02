package mctg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DamageMultiplier {

    /*
        LIST:       FIRE        WATER       NORMAL      ICE         WIND
        FIRE         1           0,5           2         2            0,8
        WATER        2            1           0,5       0,333        1,25
        NORMAL      0,5           2            1        0,8         0,909
        ICE         0,5           3          1,25        1            1
        WIND       1,25          0,8          1,1        1            1
     */

    /*
    EG.:
    FIRE - WATER, FIRE - ICE, FIRE - NORMAL
    WATER - ICE, WATER - NORMAL,
    ICE - NORMAL
    ICE - FIRE = 1 / FIRE - ICE -> You can leave that. Therefore you only need half of the connections
     */
    private static List<ElementWeakness> elementMultipliers = new ArrayList<>(Arrays.asList(
            new ElementWeakness(Element.FIRE, Element.WATER, 0.5),
            new ElementWeakness(Element.FIRE, Element.NORMAL, 2),
            new ElementWeakness(Element.FIRE, Element.ICE, 3),
            new ElementWeakness(Element.FIRE, Element.WIND, 2),
            new ElementWeakness(Element.ICE, Element.WATER, 3),
            new ElementWeakness(Element.ICE, Element.NORMAL, 1.25),
            new ElementWeakness(Element.ICE, Element.WIND, 1),
            new ElementWeakness(Element.WATER, Element.NORMAL, 0.5),
            new ElementWeakness(Element.WATER, Element.WIND, 1.25),
            new ElementWeakness(Element.WIND, Element.NORMAL, 1.1)
    ));

    /**
     * Based on the requirements of the element based damage multiplier:
     * This function returns the corresponding multiplier which is applied when the card with attackingElement
     * attacks the card with defendingElement. This function ignores the type of card (monster or spell)
     *
     * @param attackingElement is the element of the attacking card
     * @param defendingElement is the element of the defending card
     * @returns the correct multiplier in case of element1 going against element2
     */
    public static double getElementMultiplier (Element attackingElement, Element defendingElement) {

        if (attackingElement == defendingElement) {
            return 1;
        }

        for (ElementWeakness eleW: elementMultipliers) {
            // if those two elements are involved, take the multiplier based on the position of the element (attacking/defending)
            if (eleW.getAttacker() == attackingElement && eleW.getDefender() == defendingElement) {
                return eleW.getMultiplier();
            } else if (eleW.getAttacker() == defendingElement && eleW.getDefender() == attackingElement) {
                // Reciprocal(Kehrwert), if the elements roles are swapped
                return 1 / eleW.getMultiplier();
            }
        }

        return 0;
    }
}
