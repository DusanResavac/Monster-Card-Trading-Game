package http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    public final static String httpVersion = "HTTP/1.1";
    public final static Map<Integer, String> responseCodes = Map.ofEntries(
            Map.entry(200, "OK"),
            Map.entry(201, "Created"),
            Map.entry(400, "Bad Request"),
            Map.entry(404, "Not Found")

    );
    public static Map<String, String> responseHeader = Map.ofEntries(
            Map.entry("access-control-allow-origin", "*"),
            Map.entry("Server", "Serverdominator3000"),
            Map.entry("Content-Type", "text/plain")
    );

    public static String getDefaultHeader () {
        StringBuilder result = new StringBuilder();
        for (String key: responseHeader.keySet()) {
            result.append(key).append(": ").append(responseHeader.get(key)).append(System.lineSeparator());
        }
        return result.toString();
    }

    public static synchronized String getResponseHeader (Integer status, int contentLength) {
        return  httpVersion + " " + status + " " + responseCodes.get(status) + System.lineSeparator() +
                getDefaultHeader() + "Content-Length: " + contentLength + System.lineSeparator() +
                System.lineSeparator();
    }

    private HashMap<String, String> header = new HashMap<>();

    public String getValue(String key) {
        return header.get(key);
    }

    public void setValue (String key, String value) {
        header.put(key, value);
    }

    public static synchronized void writeToSocket (Integer status, String message, BufferedWriter out) {
        try {
            out.write(getResponseHeader(status, message.length()));
            out.write(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString () {
        StringBuilder result = new StringBuilder();

        for (String name: header.keySet()) {
            result.append(name).append(" : ").append(header.get(name)).append(System.lineSeparator());
        }

        return result.toString();
    }
}
