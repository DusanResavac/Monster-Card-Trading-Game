
public class DamageMultiplier {

    /**
     * Based on the requirements of the element based damage multiplier:
     * This function returns the corresponding multiplier which is applied when the card with element1
     * attacks the card with element2. This function ignores the type of card (monster or spell)
     *
     * @param element1 is the element of the attacking card
     * @param element2 is the element of the defending card
     * @returns the correct multiplier in case of element1 going against element2
     */
    public static double getElementMultiplier (Element element1, Element element2) {

        if (element1 == element2) {
            return 1;
        }

        switch (element1) {
            case FIRE:
                return element2 == Element.WATER ? 0.5 : 2;
            case WATER:
                return element2 == Element.NORMAL ? 0.5 : 2;
            case NORMAL:
                return element2 == Element.FIRE ? 0.5 : 2;
        }

        return 0;
    }
}
