package mctg.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    public final static String httpVersion = "HTTP/1.1";
    private HashMap<String, String> header = new HashMap<>();
    public final static Map<Integer, String> responseCodes = Map.ofEntries(
            Map.entry(200, "OK"),
            Map.entry(201, "Created"),
            Map.entry(204, "No Content"),
            Map.entry(400, "Bad Request"),
            Map.entry(401, "Unauthorized"),
            Map.entry(402, "Payment Required"),
            Map.entry(404, "Not Found"),
            Map.entry(500, "Internal Server Error")

    );
    public static Map<String, String> responseHeader = Map.ofEntries(
            Map.entry("access-control-allow-origin", "*"),
            Map.entry("Server", "Serverdominator3000"),
            Map.entry("Content-Type", "text/plain; charset=utf-8")
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


    public String getValue(String key) {
        return header.get(key);
    }

    public void setValue (String key, String value) {
        header.put(key, value);
    }

    public static synchronized void writeToSocket (Integer status, String message, BufferedWriter out) {
        try {
            // Ohne getBytes werden Unicode Characters nicht richtg für HTTP Response-Längen gewertet z.B.: 🟊🟊🟊
            out.write(getResponseHeader(status, message.getBytes().length + System.lineSeparator().getBytes().length));
            out.write(message + System.lineSeparator());
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
