package mctg.http.RequestHandler;

import mctg.Card;
import mctg.TradeOffer;
import mctg.http.HTTPServer;
import mctg.http.Jackson.UserRecord;
import mctg.http.RequestContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.List;

public class GetHandler extends Thread {

    private final RequestContext rc;
    private final BufferedWriter out;
    private boolean result = false;

    public GetHandler (RequestContext rc, BufferedWriter out) {
        this.rc = rc;
        this.out = out;
    }

    @Override
    public void run() {
        String[] parts = rc.getValue("url").split("/");

        if (parts.length < 2 || rc.getValue("Authorization") == null) {
            RequestContext.writeToSocket(400, "Either the requested URL is malformed or you didn't specify " +
                    "an authorization token, when one was expected", out);
            result = false;
            return;

            //return false;
        }
        if (parts[1].equalsIgnoreCase("cards") || parts[1].startsWith("deck")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            // if the deck is supposed to be listed, the second argument is going to be true and therefore only show the cards in the deck
            List<Card> cards = HTTPServer.db.getCards(rc.getValue("Authorization"), parts[1].startsWith("deck"));
            StringBuilder cardToString = new StringBuilder();
            boolean isTerminal = rc.getValue("User-Agent").startsWith("curl");

            if (cards == null) {
                RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
                result = false;
                return;
            }

            for (Card card: cards) {
                if (parts[1].equalsIgnoreCase("deck?format=plain")) {
                    cardToString.append(card.toStringPlain()).append(System.lineSeparator());
                } else {
                    cardToString.append(isTerminal ? card.toStringTerminal() : card).append(System.lineSeparator());
                }
            }
            RequestContext.writeToSocket(200, cardToString.toString(), out);
            result = true;
            return;
        }
        else if (parts[1].equalsIgnoreCase("users")) {
            if (parts.length < 3) {
                RequestContext.writeToSocket(400, "You are missing the username in the url. Correct example: server/users/altenhof", out);
                result = false;
                return;
            }
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            UserRecord user = HTTPServer.db.getUserData(rc.getValue("Authorization"), parts[2]);
            if (user != null) {
                RequestContext.writeToSocket(200, String.format("Name: %s | Bio: %s | Image: %s", user.Name(), user.Bio(), user.Image()), out);
                result = true;
                return;
            }
            RequestContext.writeToSocket(400, "An error occurred. Please check your token and the specified username", out);
            result = false;
            return;
        }
        else if (parts[1].equalsIgnoreCase("stats")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            String response = HTTPServer.db.getStats(rc.getValue("Authorization"));
            if (response != null) {
                RequestContext.writeToSocket(200, response, out);
                result = true;
                return;
            }
            RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
            result = false;
            return;
        }
        else if (parts[1].equalsIgnoreCase("score")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            String response = HTTPServer.db.showScoreboard(rc.getValue("Authorization"));
            if (response != null) {
                RequestContext.writeToSocket(200, response, out);
                result = true;
                return;
            }
            RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
            result = false;
            return;

        }
        else if (parts[1].equalsIgnoreCase("tradings")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            List<TradeOffer> tradeOffers = HTTPServer.db.getTradingOffers(rc.getValue("Authorization"));
            if (tradeOffers == null) {
                RequestContext.writeToSocket(404, HTTPServer.UNAUTHORIZED, out);
                result = false;
                return;
            } else {
                StringBuilder response = new StringBuilder();
                response.append("        Card         |  wanted  |  dmg  |                 trade-ID             |     username     ").append(System.lineSeparator());
                for (TradeOffer tradeOffer: tradeOffers) {
                    Card card = tradeOffer.getCard();
                    response.append(String.format("%s | %-8s | %5.1f | %36s | %s", card.toStringShort(), tradeOffer.getType(),
                            tradeOffer.getMinimumDamage(), tradeOffer.getId(), tradeOffer.getUsername()))
                            .append(System.lineSeparator());
                }
                RequestContext.writeToSocket(200, response.toString(), out);
                result = true;
                return;
            }
        }

        RequestContext.writeToSocket(400, "Could not parse request. Please check whether the url is correct.", out);
        result = false;
    }

    public boolean getResult() {
        return result;
    }

}
