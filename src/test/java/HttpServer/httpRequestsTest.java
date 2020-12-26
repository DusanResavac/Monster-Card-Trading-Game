package HttpServer;

import mctg.http.RequestContext;
import mctg.http.SocketInterface;
import org.junit.jupiter.api.BeforeEach;

import java.io.*;
import java.net.SocketAddress;

import static org.mockito.Mockito.mock;


public class httpRequestsTest {

    private ByteArrayOutputStream response;
    private SocketAddress addr;
    private ByteArrayInputStream request;
    private SocketInterface socket;
    private RequestContext rc;
    private BufferedReader in;
    private BufferedWriter out;

    @BeforeEach
    void beforeEach () throws IOException {

    }
}
