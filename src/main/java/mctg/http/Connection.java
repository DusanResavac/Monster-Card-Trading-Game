package mctg.http;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mctg.Card;
import mctg.http.Jackson.CardRecord;
import mctg.http.Jackson.UserRecord;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
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
            return false;
        }
        if (parts[1].equalsIgnoreCase("cards") || parts[1].equalsIgnoreCase("deck")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));
            // if the deck is supposed to be listed, the second argument is going to be true and therefore only show the cards in the deck
            List<Card> cards = HTTPServer.db.getCards(rc.getValue("Authorization"), parts[1].equals("deck"));
            StringBuilder cardToString = new StringBuilder();
            boolean isTerminal = rc.getValue("User-Agent").startsWith("curl");

            if (cards == null) {
                RequestContext.writeToSocket(401, HTTPServer.UNAUTHORIZED, out);
                return false;
            }

            for (Card card: cards) {
                cardToString.append(isTerminal ? card.toStringTerminal() : card).append(System.lineSeparator());
            }
            RequestContext.writeToSocket(200, cardToString.toString(), out);
            return true;
        }

        RequestContext.writeToSocket(400, "Could not parse request. Please check whether the url is correct.", out);
        return false;
    }

    public synchronized boolean handlePost(RequestContext rc, BufferedReader in, BufferedWriter out) {
        String[] parts = rc.getValue("url").split("/");

        if (parts.length < 2 || (rc.getValue("Authorization") == null && !parts[1].equalsIgnoreCase("users") && !parts[1].equalsIgnoreCase("sessions"))) {
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
            String response = HTTPServer.db.buyPackage(rc.getValue("Authorization"));
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
                    RequestContext.writeToSocket(204, "There are no more packages left. Please wait for the next drop of loot.", out);
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
                return HTTPServer.db.updateDeck(rc.getValue("Authorization"), cardIds);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }


        //curl -X PUT http://localhost:10001/deck --header "Content-Type: application/json" --header "Authorization: Basic kienboec-mtcgToken" -d "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]"
        return false;
    }

    public synchronized boolean handleDelete(RequestContext rc, BufferedWriter out) {
        String[] parts = rc.getValue("url").split("/");
        return false;
    }
}
