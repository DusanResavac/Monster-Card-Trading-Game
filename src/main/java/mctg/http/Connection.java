package mctg.http;

import java.io.*;
import java.net.Socket;

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
            getRequestHeader(rc, in , out);
            handleRequest (rc, in, out);
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
        String[] parts = rc.getValue("url").split("/");


        return switch (method) {
            case "GET" -> handleGet(parts, out);
            case "POST" -> handlePost(rc, in, out);
            case "PUT" -> handlePut(rc, in, out);
            case "DELETE" -> handleDelete(parts, out);
            default -> false;
        };

    }

    public synchronized boolean handleGet(String[] parts, BufferedWriter out) {
        //System.out.println(Arrays.toString(parts));
        return false;
    }

    public synchronized boolean handlePost(RequestContext rc, BufferedReader in, BufferedWriter out) {
        //System.out.println(s.getRemoteSocketAddress().toString() + " - Posting message...");
        String[] parts = rc.getValue("url").split("/");

        return false;
    }

    public synchronized boolean handlePut(RequestContext rc, BufferedReader in, BufferedWriter out) {
        String[] parts = rc.getValue("url").split("/");

        return false;
    }

    public synchronized boolean handleDelete(String[] parts, BufferedWriter out) {

        return false;
    }
}
