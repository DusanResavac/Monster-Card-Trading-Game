
import mctg.http.RequestContext;
import mctg.http.SocketInterface;
import org.junit.jupiter.api.BeforeEach;

import java.io.*;
import java.net.SocketAddress;


public class httpRequestsTest {

    private ByteArrayOutputStream response;
    private SocketAddress addr;
    private ByteArrayInputStream request;
    private SocketInterface socket;
    private RequestContext rc;
    private BufferedReader in;
    private BufferedWriter out;

    /*
        Zu großer Aufwand: Schneller und einfacher mit integration-tests...
        Mockito wurde bereits bei HTTP-Server Abgabe verwendet.
        Siehe Github https://github.com/DusanResavac/Monster-Card-Trading-Game      |   REST_HTTP_Webservices Branch
     */

    @BeforeEach
    void beforeEach () throws IOException {

    }
}
