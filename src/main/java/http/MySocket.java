package http;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MySocket implements SocketInterface {

    private Socket socket;

    public MySocket (Socket s) {
        socket = s;
    }

    public MySocket() {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
