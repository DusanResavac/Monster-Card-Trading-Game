package mctg;

public class DamageMultiplier {

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

        switch (attackingElement) {
            case FIRE:
                return defendingElement == Element.WATER ? 0.5 : 2;
            case WATER:
                return defendingElement == Element.NORMAL ? 0.5 : 2;
            case NORMAL:
                return defendingElement == Element.FIRE ? 0.5 : 2;
        }

        return 0;
    }
}
