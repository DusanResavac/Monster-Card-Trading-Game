package mctg.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public interface SocketInterface {
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
    SocketAddress getRemoteSocketAddress();
    void close() throws IOException;
}
