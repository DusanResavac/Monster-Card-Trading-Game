package mctg.http;

import mctg.http.RequestHandler.DeleteHandler;
import mctg.http.RequestHandler.GetHandler;
import mctg.http.RequestHandler.PostHandler;
import mctg.http.RequestHandler.PutHandler;

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
        GetHandler getHandler = new GetHandler(rc, out);
        getHandler.start();
        try {
            getHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getHandler.getResult();
        //return GetHandler.handleGet(rc, out);
    }

    public synchronized boolean handlePost(RequestContext rc, BufferedReader in, BufferedWriter out) {
        PostHandler postHandler = new PostHandler(rc, in, out);
        postHandler.start();
        try {
            postHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return postHandler.getResult();
    }

    public synchronized boolean handlePut(RequestContext rc, BufferedReader in, BufferedWriter out) {
        PutHandler putHandler = new PutHandler(rc, in, out);
        putHandler.start();
        try {
            putHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return putHandler.getResult();
        //return PutHandler.handlePut(rc, in, out);
    }

    public synchronized boolean handleDelete(RequestContext rc, BufferedWriter out) {
        DeleteHandler deleteHandler = new DeleteHandler(rc, out);
        deleteHandler.start();
        try {
            deleteHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return deleteHandler.getResult();
        //return DeleteHandler.handleDelete(rc, out);
    }
}
