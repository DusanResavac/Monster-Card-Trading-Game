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
    public static Database db;
    /**
     * Queue for users who want to battle. If no users are present, a new battleEntry with the token and corresponding
     * account data is created (See BattleEntry). Each BattleEntry has a String as its value which can be in three possible states
     *  1. null -> This user does not have a battle partner yet
     *  2. "taken" -> Someone is currently generating a battle with this user
     *  3. String with battleLog -> Someone already held a battle with this person and this entry is going to be deleted
     *     after the waiting person received its battleLog
     */
    public static final HashMap<BattleEntry, String> usersInQueue = new HashMap<>();
    public static final String UNAUTHORIZED = "Token is no longer valid, please try logging into your account.";

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
