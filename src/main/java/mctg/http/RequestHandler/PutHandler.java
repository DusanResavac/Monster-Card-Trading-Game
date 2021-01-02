package mctg.http.RequestHandler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mctg.http.HTTPServer;
import mctg.http.Jackson.UserRecord;
import mctg.http.RequestContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PutHandler extends Thread {

    private final RequestContext rc;
    private final BufferedReader in;
    private final BufferedWriter out;
    private boolean result = false;

    public PutHandler (RequestContext rc, BufferedReader in, BufferedWriter out) {
        this.rc = rc;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        String[] parts = rc.getValue("url").split("/");
        if (parts.length < 2 || rc.getValue("Authorization") == null) {
            result = false;
            return;
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
            result = false;
            return;
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
                    RequestContext.writeToSocket(200, "Everything is A-Ok" + System.lineSeparator(), out);
                } else {
                    RequestContext.writeToSocket(400, "Didn't alter deck. " +
                            "Please check that you have selected 4 cards which are in your possession", out);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        else if (parts[1].equalsIgnoreCase("users")) {
            if (parts.length < 3) {
                RequestContext.writeToSocket(400, "You are missing the username in the url. Correct example: server/users/altenhof", out);
                result = false;
                return;
            }
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));

            try {
                UserRecord user = mapper.readValue(messageStr, new TypeReference<UserRecord>() {});
                // just to make sure, that the user doesn't try to update his elo or coins ;)
                user = new UserRecord(user.Username(), user.Password(), user.Name(), user.Bio(), user.Image(), null, null, null, null);
                if (HTTPServer.db.updateUser(user, parts[2], rc.getValue("Authorization"))) {
                    RequestContext.writeToSocket(200, "User was updated", out);
                    result = true;
                    return;
                } else {
                    RequestContext.writeToSocket(200, "An error occurred. Please check the specified token and username", out);
                    result = false;
                    return;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }

        RequestContext.writeToSocket(400, "Could not parse request. Please check whether the url is correct.", out);
        result = false;
    }

    public boolean getResult() {
        return result;
    }

}
