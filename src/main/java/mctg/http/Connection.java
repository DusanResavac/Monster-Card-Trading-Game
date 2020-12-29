package mctg.http;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mctg.BattleEntry;
import mctg.Card;
import mctg.User;
import mctg.http.Jackson.CardRecord;
import mctg.http.Jackson.UserRecord;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Connection extends Thread implements ConnectionInterface {

    private SocketInterface s;


    public Connection (Socket s) {
        this.s = new MySocket(s);
    }

    public Connection (SocketInterface si) {
        this.s = si;
    }


    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            RequestContext rc = new RequestContext();
            if (!getRequestHeader(rc, in)) {
                return;
            }
            handleRequest (rc, in, out);
            out.close();
            in.close();
            s.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean getRequestHeader (RequestContext rc, BufferedReader in) throws IOException {

        // read first row (Request Method and HTTP Version)
        String line = in.readLine();
        if (line == null) {
            return false;
        }
        String[] parts = line.split(" ");
        // first one is method
        rc.setValue("method", parts[0].trim());
        // last one is HTTP version
        rc.setValue("httpVersion", parts[parts.length-1].trim());
        StringBuilder url = new StringBuilder();
        // everything in between is part of the URL
        for (int i = 1; i < parts.length-1; i++) {
            url.append(parts[i]).append(" ");
        }
        rc.setValue("url", url.toString().trim());


        // ODER: Alles bis zum ersten Leerzeichen ist Method, beim letzten Leerzeichen beginnt die HTTP Version
            /*
            line = line.trim();
            rc.setValue("method", line.substring(0, line.indexOf(" ")));
            rc.setValue("httpVersion", line.substring(line.lastIndexOf(" ") + 1));
            rc.setValue("url", line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" ")));
            */


        //System.out.println(line);
        while ((line = in.readLine()) != null) {

            if (line.equals(System.lineSeparator()) || line.equals("")) {

                return true;
            } else {
                parts = line.split(":");
                rc.setValue(parts[0].trim(), parts[1].trim());
            }

        }
        return true;
    }


    public boolean handleRequest(RequestContext rc, BufferedReader in, BufferedWriter out) {
        String method = rc.getValue("method");

        return switch (method) {
            case "GET" -> handleGet(rc, out);
            case "POST" -> handlePost(rc, in, out);
            case "PUT" -> handlePut(rc, in, out);
            case "DELETE" -> handleDelete(rc, out);
            default -> false;
        };

    }

    public synchronized boolean handleGet(RequestContext rc, BufferedWriter out) {
        //System.out.println(Arrays.toString(parts));
        String[] parts = rc.getValue("url").split("/");

        if (parts.length < 2 || rc.getValue("Authorization") == null) {
            RequestContext.writeToSocket(400, "Either the requested URL is malformed or you didn't specify an authorization token, when one was expected", out);
            return false;
        }
        if (parts[1].equalsIgnoreCase("cards") || parts[1].startsWith("deck")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            // if the deck is supposed to be listed, the second argument is going to be true and therefore only show the cards in the deck
            List<Card> cards = HTTPServer.db.getCards(rc.getValue("Authorization"), parts[1].startsWith("deck"));
            StringBuilder cardToString = new StringBuilder();
            boolean isTerminal = rc.getValue("User-Agent").startsWith("curl");

            if (cards == null) {
                RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
                return false;
            }

            for (Card card: cards) {
                if (parts[1].equalsIgnoreCase("deck?format=plain")) {
                    cardToString.append(card.toStringPlain()).append(System.lineSeparator());
                } else {
                    cardToString.append(isTerminal ? card.toStringTerminal() : card).append(System.lineSeparator());
                }
            }
            RequestContext.writeToSocket(200, cardToString.toString(), out);
            return true;
        }
        else if (parts[1].equalsIgnoreCase("users")) {
            if (parts.length < 3) {
                RequestContext.writeToSocket(400, "You are missing the username in the url. Correct example: server/users/altenhof", out);
                return false;
            }
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            UserRecord user = HTTPServer.db.getUserData(rc.getValue("Authorization"), parts[2]);
            if (user != null) {
                RequestContext.writeToSocket(200, String.format("Name: %s | Bio: %s | Image: %s", user.Name(), user.Bio(), user.Image()), out);
                return true;
            }
            RequestContext.writeToSocket(400, "An error occurred. Please check your token and the specified username", out);
            return false;
        }
        else if (parts[1].equalsIgnoreCase("stats")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            String response = HTTPServer.db.getStats(rc.getValue("Authorization"));
            if (response != null) {
                RequestContext.writeToSocket(200, response, out);
                return true;
            }
            RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
            return false;
        }
        else if (parts[1].equalsIgnoreCase("score")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            String response = HTTPServer.db.showScoreboard(rc.getValue("Authorization"));
            if (response != null) {
                RequestContext.writeToSocket(200, response, out);
                return true;
            }
            RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
            return false;

        }

        RequestContext.writeToSocket(400, "Could not parse request. Please check whether the url is correct.", out);
        return false;
    }

    public synchronized boolean handlePost(RequestContext rc, BufferedReader in, BufferedWriter out) {
        String[] parts = rc.getValue("url").split("/");

        if (parts.length < 2 || (rc.getValue("Authorization") == null && !parts[1].equalsIgnoreCase("users") && !parts[1].equalsIgnoreCase("sessions"))) {
            RequestContext.writeToSocket(400, "Either the requested URL is incorrect or you didn't specify an authorization token, when one was expected", out);
            return false;
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
                return false;
            }
        }
        String messageStr = message.toString();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (parts[1].equalsIgnoreCase("packages")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            try {
                ArrayList<CardRecord> cardRecords = mapper.readValue(messageStr, new TypeReference<ArrayList<CardRecord>>(){} );
                return HTTPServer.db.insertPackage(cardRecords, rc.getValue("Authorization"));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("transactions") && parts[2].equalsIgnoreCase("packages")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            String response = HTTPServer.db.buyPackage(rc.getValue("Authorization"), rc.getValue("User-Agent").startsWith("curl"));
            if (response == null) {
                return false;
            }
            switch (response) {
                case ("coins"):
                    RequestContext.writeToSocket(402, "You don't have any coins left. Try visiting our shop to acquire more coins and get those super rare cards! ;)", out);
                    break;
                case ("token"):
                    RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
                    break;
                case ("package"):
                    RequestContext.writeToSocket(404, "There are no more packages left. Please wait for the next drop of loot.", out);
                    break;
                default:
                    RequestContext.writeToSocket(200, response, out);
                    return true;
            }
        }
        else if (parts[1].equalsIgnoreCase("users")) {
            try {
                UserRecord userRecord = mapper.readValue(messageStr, new TypeReference<UserRecord>() {});
                if (HTTPServer.db.insertUsers(userRecord)) {
                    RequestContext.writeToSocket(201, "User " + userRecord.Username() + " added", out);
                    return true;
                } else {
                    RequestContext.writeToSocket(401, "An error occurred.", out);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("sessions")) {
            try {
                UserRecord userRecord = mapper.readValue(messageStr, new TypeReference<UserRecord>() {});
                if (userRecord.Username() == null || userRecord.Password() == null) {
                    RequestContext.writeToSocket(400, "Username or Password is missing", out);
                    return false;
                }
                String response = HTTPServer.db.loginUser(userRecord.Username(), userRecord.Password());
                if (response == null) {
                    RequestContext.writeToSocket(401, "User not found or combination of username and password is wrong.", out);
                } else {
                    RequestContext.writeToSocket(200, response, out);
                    return true;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("battles")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            UserRecord user = HTTPServer.db.getUserData(rc.getValue("Authorization"), null);
            if (user == null) {
                RequestContext.writeToSocket(400, HTTPServer.UNAUTHORIZED, out);
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

        }

        return false;
    }

    public synchronized boolean handlePut(RequestContext rc, BufferedReader in, BufferedWriter out) {
        String[] parts = rc.getValue("url").split("/");
        if (parts.length < 2 || rc.getValue("Authorization") == null) {
            return false;
        }

        int counter = 0;
        int contentLength = Integer.parseInt(rc.getValue("Content-Length"));
        StringBuilder message = new StringBuilder();
        try {
            while (counter < contentLength) {
                message.append((char) in.read());
                counter++;
            }
        } catch (IOException ioException) {
            return false;
        }

        String messageStr = message.toString();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (parts[1].equalsIgnoreCase("deck")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            try {
                ArrayList<String> cardIds = mapper.readValue(messageStr, new TypeReference<ArrayList<String>>() {});
                if (HTTPServer.db.updateDeck(rc.getValue("Authorization"), cardIds)) {
                    RequestContext.writeToSocket(200, "Everything is A-Ok", out);
                } else {
                    RequestContext.writeToSocket(400, "Didn't alter deck. Please check that you have selected 4 cards which are in your possession", out);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("users")) {
            if (parts.length < 3) {
                RequestContext.writeToSocket(400, "You are missing the username in the url. Correct example: server/users/altenhof", out);
                return false;
            }
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));

            try {
                UserRecord user = mapper.readValue(messageStr, new TypeReference<UserRecord>() {});
                // just to make sure, that the user doesn't try to update his elo or coins ;)
                user = new UserRecord(user.Username(), user.Password(), user.Name(), user.Bio(), user.Image(), null, null, null, null);
                if (HTTPServer.db.updateUser(user, parts[2], rc.getValue("Authorization"))) {
                    RequestContext.writeToSocket(200, "User was updated", out);
                    return true;
                } else {
                    RequestContext.writeToSocket(200, "An error occurred. Please check the specified token and username", out);
                    return false;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }

        RequestContext.writeToSocket(400, "Could not parse request. Please check whether the url is correct.", out);
        return false;
    }

    public synchronized boolean handleDelete(RequestContext rc, BufferedWriter out) {
        String[] parts = rc.getValue("url").split("/");
        return false;
    }
}
