package http;

import java.util.HashMap;

public class RequestContext {

    private HashMap<String, String> header = new HashMap<>();

    public String getValue(String key) {
        return header.get(key);
    }

    public void setValue (String key, String value) {
        header.put(key, value);
    }

    public String toString () {
        StringBuilder result = new StringBuilder();

        for (String name: header.keySet()) {
            result.append(name).append(" : ").append(header.get(name)).append(System.lineSeparator());
        }

        return result.toString();
    }
}
