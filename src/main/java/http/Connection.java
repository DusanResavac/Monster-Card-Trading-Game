package http;

import lombok.SneakyThrows;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

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
            System.out.println("New HTTP-Request: \r\n=======================");
            getRequestHeader(rc, in , out);
            handleRequest (rc, in, out);
            System.out.println("=======================\r\n");
            s.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void getRequestHeader (RequestContext rc, BufferedReader in, BufferedWriter out) throws IOException {

        // read first row (Request Method and HTTP Version)
        String line = in.readLine();
        if (line == null) {
            return;
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

                return;
            } else {
                parts = line.split(":");
                rc.setValue(parts[0].trim(), parts[1].trim());
            }

        }
    }


    public boolean handleRequest(RequestContext rc, BufferedReader in, BufferedWriter out) {
        String method = rc.getValue("method");
        // Parts looks like this, when someone requests a specific message [, messages,  2]
        String[] parts = rc.getValue("url").split("/");

        // if url is missing messages, ignore request
        if (parts.length == 0 || !parts[1].equalsIgnoreCase("messages")) {
            String error = "Couldn't process request. Did you forget to add \"/messages\" ?";
            System.out.println(error);
            RequestContext.writeToSocket(400, error, out);
            return false;
        }

        return switch (method) {
            case "GET" -> handleGet(parts, out);
            case "POST" -> handlePost(rc, in, out);
            case "PUT" -> handlePut(parts, rc, in, out);
            case "DELETE" -> handleDelete(parts, out);
            default -> false;
        };

    }

    public synchronized boolean handleGet(String[] parts, BufferedWriter out) {
        //System.out.println(Arrays.toString(parts));
        if (parts.length > 2) {
            System.out.println(s.getRemoteSocketAddress().toString() + " - Reading message...");
            try {
                String message = HTTPServer.getValue(Integer.parseInt(parts[2]));
                if (message != null) {
                    RequestContext.writeToSocket(200, message, out);
                    System.out.println(message);
                    return true;
                }

                String error = "Couldn't find stated message.";
                RequestContext.writeToSocket(404, error, out);
                System.err.println(error);
                return false;


            } catch (NumberFormatException e) {
                System.err.println("Entered ID <<" + parts[2] + ">> couldn't be converted to number.");
                String error = "Invalid messageID entered. A number was expected.";
                RequestContext.writeToSocket(400, error, out);
                return false;
            }
            // print all messages
        } else {
            System.out.println(s.getRemoteSocketAddress().toString() + " - Reading messages...");
            StringBuilder responseBody = new StringBuilder();

            for (Integer key : HTTPServer.messages.keySet()) {
                responseBody.append("------------")
                        .append(System.lineSeparator())
                        .append("ID: ").append(key).append(" - ")
                        .append(HTTPServer.getValue(key))
                        .append(System.lineSeparator());
            }
            responseBody.append("------------");

            System.out.println(responseBody);
            RequestContext.writeToSocket(200, responseBody.toString(), out);
            return true;
        }
    }

    public synchronized boolean handlePost(RequestContext rc, BufferedReader in, BufferedWriter out) {
        System.out.println(s.getRemoteSocketAddress().toString() + " - Posting message...");
        StringBuilder message = new StringBuilder();
        int counter = 0;
        try {
            while (counter < Integer.parseInt(rc.getValue("Content-Length"))) {
                message.append((char) in.read());
                counter++;
            }

            HTTPServer.currentId++;
            String responseBody = "ID: " + HTTPServer.currentId;
            System.out.println(message.toString());
            HTTPServer.setValue(HTTPServer.currentId, message.toString());

            RequestContext.writeToSocket(201, responseBody, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean handlePut(String[] parts, RequestContext rc, BufferedReader in, BufferedWriter out) {
        // Falls keine ID angegeben wurde
        if (parts.length < 3) {
            String error = "Expected messageID couldn't be retrieved. Please append an ID to the request.";
            RequestContext.writeToSocket(400, error, out);
            return false;
        }

        System.out.println(s.getRemoteSocketAddress().toString() + " - Updating message...");

        try {
            Integer messageID = Integer.parseInt(parts[2]);

            if (HTTPServer.getValue(messageID) == null) {
                System.err.println("Update attempt on non-existent message.");
                RequestContext.writeToSocket(404, "Entered messageID doesn't exist. Please use /messages to get receive all messages.", out);
                return false;
            }

            StringBuilder newMessage = new StringBuilder();
            int counter = 0;


            while (counter < Integer.parseInt(rc.getValue("Content-Length"))) {
                newMessage.append((char) in.read());
                counter++;
            }

            System.out.println("Old message: ");
            System.out.println(HTTPServer.getValue(messageID));
            System.out.println(System.lineSeparator() + "New message: ");
            System.out.println(newMessage.toString());

            HTTPServer.setValue(messageID, newMessage.toString());
            RequestContext.writeToSocket(200, newMessage.toString(), out);
            return true;
        } catch (NumberFormatException e) {
            System.err.println("Entered ID <<" + parts[2] + ">> couldn't be converted to number.");

            String error = "Invalid messageID entered. A number was expected.";
            RequestContext.writeToSocket(400, error, out);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean handleDelete(String[] parts, BufferedWriter out) {
        if (parts.length < 3) {
            String error = "Expected messageID couldn't be retrieved. Please append an ID to the request.";
            RequestContext.writeToSocket(400, error, out);
            return false;
        }

        System.out.println(s.getRemoteSocketAddress().toString() + " - Deleting message...");

        try {
            Integer messageID = Integer.parseInt(parts[2]);

            if (HTTPServer.getValue(messageID) == null) {
                System.err.println("Delete attempt on non-existent message.");
                RequestContext.writeToSocket(404, "Specified message doesn't exist.", out);
                return false;
            }
            HTTPServer.removeEntry(messageID);
            System.out.println("Message " +  messageID + " deleted.");
            RequestContext.writeToSocket(200, "OK", out);
            return true;
        } catch (NumberFormatException e) {
            System.err.println("Entered ID <<" + parts[2] + ">> couldn't be converted to number.");

            String error = "Invalid messageID entered. A number was expected.";
            RequestContext.writeToSocket(400, error, out);
            return false;
        }
    }
}
