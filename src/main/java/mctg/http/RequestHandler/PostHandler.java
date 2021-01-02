package mctg.http.RequestHandler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mctg.BattleEntry;
import mctg.Card;
import mctg.http.ConnectionInterface;
import mctg.http.HTTPServer;
import mctg.http.Jackson.CardRecord;
import mctg.http.Jackson.TradeOfferRecord;
import mctg.http.Jackson.UserRecord;
import mctg.http.RequestContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostHandler extends Thread {

    private final RequestContext rc;
    private final BufferedReader in;
    private final BufferedWriter out;
    private boolean result = false;

    public PostHandler(RequestContext rc, BufferedReader in, BufferedWriter out) {
        this.rc = rc;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        String[] parts = rc.getValue("url").split("/");

        if (parts.length < 2 || (rc.getValue("Authorization") == null &&
                !parts[1].equalsIgnoreCase("users") &&
                !parts[1].equalsIgnoreCase("sessions"))) {
            RequestContext.writeToSocket(400, "Either the requested URL is incorrect or you didn't specify " +
                    "an authorization token, when one was expected", out);
            result = false;
            return;
        }


        int counter = 0;
        StringBuilder message = new StringBuilder();
        if (!parts[1].equalsIgnoreCase("battles")) {
            int contentLength = Integer.parseInt(rc.getValue("Content-Length"));
            try {
                while (counter < contentLength) {
                    message.append((char) in.read());
                    counter++;
                }
            } catch (IOException ioException) {
                result = false;
                return;
            }
        }

        String messageStr = message.toString();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);


        if (parts[1].equalsIgnoreCase("packages")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            try {
                ArrayList<CardRecord> cardRecords = mapper.readValue(messageStr, new TypeReference<ArrayList<CardRecord>>() {
                });
                if (HTTPServer.db.insertPackage(cardRecords, rc.getValue("Authorization"))) {
                    RequestContext.writeToSocket(201, "Successfully created package", out);
                    result = true;
                    return;
                } else {
                    RequestContext.writeToSocket(400, "An error occured. Please verify that there are 5 cards " +
                            "and that your token is still valid.", out);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("transactions") && parts[2].equalsIgnoreCase("packages")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            String response = HTTPServer.db.buyPackage(rc.getValue("Authorization"), rc.getValue("User-Agent").startsWith("curl"));
            if (response == null) {
                result = false;
                return;
            }
            switch (response) {
                case ("coins"):
                    RequestContext.writeToSocket(402, "You don't have any coins left. " +
                            "Try visiting our shop to acquire more coins and get those super rare cards! ;)", out);
                    break;
                case ("token"):
                    RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
                    break;
                case ("package"):
                    RequestContext.writeToSocket(404, "There are no more packages left. " +
                            "Please wait for the next drop of loot.", out);
                    break;
                default:
                    RequestContext.writeToSocket(200, response, out);
                    result = true;
                    return;
            }
        }
        else if (parts[1].equalsIgnoreCase("users")) {
            try {
                UserRecord userRecord = mapper.readValue(messageStr, new TypeReference<UserRecord>() {});
                userRecord = new UserRecord(userRecord.Username(), userRecord.Password(), userRecord.Name(), userRecord.Bio(), userRecord.Image(), 20, 100.0, 0, 0);
                if (HTTPServer.db.insertUsers(userRecord)) {
                    RequestContext.writeToSocket(201, "User " + userRecord.Username() + " added", out);
                    result = true;
                    return;
                } else {
                    RequestContext.writeToSocket(401, "An error occurred. The username is already taken.", out);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("sessions")) {
            try {
                UserRecord userRecord = mapper.readValue(messageStr, new TypeReference<UserRecord>() {
                });
                if (userRecord.Username() == null || userRecord.Password() == null) {
                    RequestContext.writeToSocket(400, "Username or Password is missing", out);
                    result = false;
                    return;
                }
                String response = HTTPServer.db.loginUser(userRecord.Username(), userRecord.Password());
                if (response == null) {
                    RequestContext.writeToSocket(401, "User not found or combination of username and password is wrong.", out);
                } else {
                    RequestContext.writeToSocket(200, response, out);
                    result = true;
                    return;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("battles")) {
            result = handleBattleRequest(rc, out);
            return;
        }
        else if (parts[1].equalsIgnoreCase("tradings")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));

            if (parts.length == 3) {

                messageStr = messageStr.replaceAll("\"", "");
                if (HTTPServer.db.tryToTrade(rc.getValue("Authorization"), parts[2], messageStr)) {
                    RequestContext.writeToSocket(200, "Successfully traded", out);
                    result = true;
                    return;
                } else {
                    RequestContext.writeToSocket(400, "An error occurred while processing the request. " +
                            "Please check the trade requirements and verify that your token is valid.", out);
                }
            } else {
                try {
                    TradeOfferRecord tradeOfferRecord = mapper.readValue(messageStr, new TypeReference<TradeOfferRecord>() {
                    });
                    if (HTTPServer.db.insertTradeOffer(rc.getValue("Authorization"), tradeOfferRecord)) {
                        RequestContext.writeToSocket(200, "Successfully inserted trade offer", out);
                        result = true;
                        return;
                    } else {
                        RequestContext.writeToSocket(400, "An error occurred while processing the request. " +
                                "Please check whether you possess the specified card and verify that your token is valid.", out);
                    }
                } catch (JsonProcessingException e) {
                    //e.printStackTrace();
                    RequestContext.writeToSocket(400, "Wrong body format", out);
                    result = false;
                    return;
                }

            }
        }

        result = false;
    }



    public boolean getResult() {
        return result;
    }

    private static boolean handleBattleRequest (RequestContext rc, BufferedWriter out) {
        rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
        UserRecord user = HTTPServer.db.getUserData(rc.getValue("Authorization"), null);
        if (user == null) {
            RequestContext.writeToSocket(400, HTTPServer.UNAUTHORIZED, out);
            return false;
        }
        List<Card> cards = HTTPServer.db.getCards(rc.getValue("Authorization"), true);
        if (cards == null || cards.size() != 4) {
            RequestContext.writeToSocket(401, "You don't have a deck compliant to the rules. Please create one, before trying to start a battle", out);
            return false;
        }

        BattleEntry enemy = null;

        synchronized (HTTPServer.usersInQueue) {
            double smallestEloDiff = Double.MAX_VALUE;
            for (BattleEntry battleEntry : HTTPServer.usersInQueue.keySet()) {
                // no battles between same accounts
                if (battleEntry.getUserRecord().Username().equals(user.Username())) {
                    continue;
                }
                // if there isn't a battle summary, then this enemy doesn't have a partner
                if (HTTPServer.usersInQueue.get(battleEntry) == null) {
                    double currDiff = Math.abs(battleEntry.getUserRecord().Elo() - user.Elo());
                    if (currDiff < smallestEloDiff) {
                        smallestEloDiff = currDiff;
                        enemy = battleEntry;
                    }
                }
            }
        }

        System.out.println(user.Username() + " joins - Enemy: " + (enemy == null ? null : enemy.getUserRecord().Username()));

        // if no battle foes are present, register yourself and wait for other players
        if (enemy == null) {
            BattleEntry battleEntry = new BattleEntry(rc.getValue("Authorization"), user);
            synchronized (HTTPServer.usersInQueue) {
                HTTPServer.usersInQueue.put(battleEntry, null);
            }
            long time = new Date().getTime();
            // the client has to wait in the worst case scenario 60 seconds, before the connection is closed
            long maxWaitTime = 1000 * 60;
            String battleResult = null;
            while ((time + maxWaitTime) >= new Date().getTime()) {
                try {
                    synchronized (HTTPServer.usersInQueue) {
                        battleResult = HTTPServer.usersInQueue.get(battleEntry);
                    }
                    // If a battleResult shows up, someone battled us or is just about to
                    if (battleResult != null) {
                        // if this Battle is about to take place, wait a little longer
                        if (battleResult.equals("taken")) {
                            maxWaitTime += 5000;
                            Thread.sleep(500);
                            continue;
                        }
                        break;
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    synchronized (HTTPServer.usersInQueue) {
                        HTTPServer.usersInQueue.remove(battleEntry);
                    }
                    e.printStackTrace();
                    return false;
                }
            }

            synchronized (HTTPServer.usersInQueue) {
                HTTPServer.usersInQueue.remove(battleEntry);
            }
            if (battleResult != null) {
                RequestContext.writeToSocket(200, battleResult, out);
                return true;
            } else {
                RequestContext.writeToSocket(404, "No battle partners were found. Please try again later.", out);
            }
        }
        else {
            // "Book" this battle
            synchronized (HTTPServer.usersInQueue) {
                HTTPServer.usersInQueue.put(enemy, "taken");
            }
            String response = HTTPServer.db.simulateBattle(rc.getValue("Authorization"), enemy.getToken());
            if (response != null) {
                synchronized (HTTPServer.usersInQueue) {
                    HTTPServer.usersInQueue.put(enemy, response);
                }

                RequestContext.writeToSocket(200, response, out);
                return true;
            }
            RequestContext.writeToSocket(500, "An error occured while simulating a battle. Please try again later.", out);
        }

        return false;
    }



}
