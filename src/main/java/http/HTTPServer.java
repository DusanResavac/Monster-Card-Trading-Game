package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {
    public static void main(String[] args) {
        HTTPServer server = new HTTPServer(80);
    }


    public HTTPServer (int port) {
        try {

                ServerSocket listener = new ServerSocket();
                listener.bind(new InetSocketAddress(port));
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

}
