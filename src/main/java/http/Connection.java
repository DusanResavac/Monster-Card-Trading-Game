package http;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

public class Connection extends Thread {

    private Socket s;
    private int currentId = 4;
    private static HashMap<Integer, String> messages = new HashMap<>();

    public Connection (Socket s) {
        this.s = s;
        setValue(1, "Erste Nachricht");
        setValue(2, "Zweite Nachricht");
        setValue(3, "Dritte Nachricht");
        setValue(4, "Vierte Nachricht");
    }

    public void setValue (Integer key, String value) {
        if (key == null) {
            return;
        }
        messages.put(key, value);
    }

    public String getValue (Integer key) {
        return messages.get(key);
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            // read first row (Request Method and HTTP Version)
            String line = in.readLine();
            if (line == null) {
                return;
            }
            RequestContext rc = new RequestContext();

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
                    //System.out.println("Empty line");
                    System.out.println(rc);
                    handleRequest (rc, in, out);

                    break;
                } else {
                    parts = line.split(":");
                    rc.setValue(parts[0].trim(), parts[1].trim());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleRequest(RequestContext rc, BufferedReader in, BufferedWriter out) throws IOException {
        String method = rc.getValue("method");
        String[] parts = rc.getValue("url").split("/");
        
        out.write("HTTP/1.1");
        out.flush();

        // if url is missing messages, ignore request
        if (parts.length == 0 || !parts[1].equalsIgnoreCase("messages")) {
            return;
        }

        if (method.equals("GET")) {
            // Parts looks like this, when someone requests a specific message [, messages,  2]
            System.out.println("parts:" + Arrays.toString(parts));
            if (parts[1].equals("messages")) {
                // print specific message
                if (parts.length > 2) {
                    try {
                        System.out.println(getValue(Integer.parseInt(parts[2].trim())));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    // print all messages
                } else {
                    for (Integer key : messages.keySet()) {
                        System.out.println(messages.get(key));
                    }
                }
            }
        }

        if (method.equals("POST")) {
            StringBuilder message = new StringBuilder();
            int counter = 0;
            try {
                while (counter < Integer.parseInt(rc.getValue("Content-Length"))) {
                    message.append((char) in.read());
                    counter++;
                }
                currentId++;
                messages.put(currentId, message.toString());
                out.write(currentId);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

}
