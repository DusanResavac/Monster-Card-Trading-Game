package mctg.http;

import mctg.BattleEntry;
import mctg.database.Database;
import mctg.http.Jackson.UserRecord;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPServer {
    public static void main(String[] args) {
        HTTPServer server = new HTTPServer(10001);
    }

    public static int currentId = 0;
    protected static HashMap<Integer, String> messages = new HashMap<>();
    protected static Database db;
    protected static final HashMap<BattleEntry, String> usersInQueue = new HashMap<>();
    protected static final String UNAUTHORIZED = "Token is no longer valid, please try logging into your account.";

    public HTTPServer (int port) {
        try {
            ServerSocket listener = new ServerSocket();
            listener.bind(new InetSocketAddress(port));
            System.out.println("Listening for Connections on port " + port + " ...");
            db = new Database();
            db.openConnection("jdbc:postgresql://localhost:5432/mctg", "postgres", "password");
            while (true) {
                Socket s = listener.accept();
                Connection c = new Connection(s);
                c.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
