package mctg;

import java.util.List;
import java.util.Random;

public class Battle {

    private List<Card> deck1;
    private List<Card> deck2;
    private String username1;
    private String username2;
    private int rounds = 0;

    public Battle(List<Card> deck1, List<Card> deck2, String username1, String username2) {
        this.deck1 = deck1;
        this.deck2 = deck2;
        this.username1 = username1;
        this.username2 = username2;
    }

    private double calculateRound () {
        Random ran = new Random();
        Card card1 = deck1.get(ran.nextInt(deck1.size()));
        Card card2 = deck1.get(ran.nextInt(deck2.size()));
        return card1.calculateIncomingDamage(card2) - card2.calculateIncomingDamage(card1);
    }

    /**
     * Transfers the card with the specified id from one deck to another
     * @param loser the deck, which loses a card
     * @param winner the deck, which gains a card
     * @param id the id, which is linked to the card, that is supposed to be transfered
     */
    private void transferCardToDeck (List<Card> loser, List<Card> winner, String id) {
        for (Card card: loser) {
            if (card.getId().equals(id)) {
                winner.add(card);
                loser.remove(card);
                return;
            }
        }
    }

    public String startBattle () {
        StringBuilder summary = new StringBuilder();
        // starting attacker is being selected randomly
        boolean player1Attacks = Math.random() >= 0.5;
        summary.append("Battle begins: Starting attacker: ").append(player1Attacks ? username1 : username2).append(" against ").append(player1Attacks ? username2 : username1).append(System.lineSeparator());
        while (rounds < 100 && deck1.size() > 0 && deck2.size() > 0) {
            Random ran = new Random();
            Card card1 = deck1.get(ran.nextInt(deck1.size()));
            Card card2 = deck2.get(ran.nextInt(deck2.size()));
            // if result > 0, then card1 received more damage and therefore loses
            double roundResult = card1.calculateIncomingDamage(card2) - card2.calculateIncomingDamage(card1);

            summary.append(String.format("%s vs %s ---- ", card1.toStringShort(),  card2.toStringShort()));

            if (roundResult == 0) {
                // Ties never led to a winner, that's why I implemented the attacker's advantage,
                // that was part of the project at the beginning and was later removed
                //summary.append("TIE");
                if (player1Attacks) {
                    transferCardToDeck(deck2, deck1, card2.getId());
                } else {
                    transferCardToDeck(deck1, deck2, card1.getId());
                }
                summary.append("TIE - Attacker: ")
                        .append(player1Attacks ? username1 : username2)
                        .append(" wins | Remaining cards: ")
                        .append(username1)
                        .append(": ")
                        .append(deck1.size())
                        .append(" - ")
                        .append(username2)
                        .append(": ")
                        .append(deck2.size());
            }
            if (roundResult > 0) {
                transferCardToDeck(deck1, deck2, card1.getId());
                summary.append(username2).append(" wins | Remaining cards: ").append(username1).append(": ").append(deck1.size()).append(" - ")
                        .append(username2).append(": ").append(deck2.size());
            }
            if (roundResult < 0) {
                transferCardToDeck(deck2, deck1, card2.getId());
                summary.append(username1).append(" wins | Remaining cards: ").append(username1).append(": ").append(deck1.size()).append(" - ")
                        .append(username2).append(": ").append(deck2.size());
            }
            rounds++;
            summary.append(System.lineSeparator());
            player1Attacks = !player1Attacks;
        }
        if (rounds == 100 && deck1.size() > 0 && deck2.size() > 0) {
            summary.append("TIE --- NO WINNER");
        } else if (deck1.size() == 0) {
            summary.append(username2).append(" wins");
        } else if (deck2.size() == 0) {
            summary.append(username1).append(" wins");
        }
        summary.append(System.lineSeparator());

        return summary.toString();
    }
}
