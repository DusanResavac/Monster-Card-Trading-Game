package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class HTTPServer {
    public static void main(String[] args) {
        HTTPServer server = new HTTPServer(80);
    }

    protected static int currentId = 4;
    protected static HashMap<Integer, String> messages = new HashMap<>();

    public HTTPServer (int port) {
        try {

            ServerSocket listener = new ServerSocket();
            listener.bind(new InetSocketAddress(port));
            setValue(1, "Erste Nachricht");
            setValue(2, "Zweite Nachricht");
            setValue(3, "Dritte Nachricht");
            setValue(4, "Vierte Nachricht");
            System.out.println("Listening for Connections on port " + port + " ...");
            while (true) {
                Socket s = listener.accept();
                Connection c = new Connection(s);
                c.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setValue (Integer key, String value) {
        if (key == null) {
            return;
        }
        messages.put(key, value);
    }

    public static String getValue (Integer key) {
        return messages.get(key);
    }

    public static void removeEntry(Integer key) { messages.remove(key); }

}
