package HttpServer;

import http.Connection;
import http.HTTPServer;
import http.RequestContext;
import http.SocketInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
        HTTPServer.setValue(1, "First");
        HTTPServer.setValue(2, "Second");
        HTTPServer.setValue(3, "Third");
        HTTPServer.setValue(4, "Fourth");
        HTTPServer.currentId = 4;

        response = new ByteArrayOutputStream();
        addr = new InetSocketAddress (InetAddress.getByName("127.0.0.1"), 7856);
        request = new ByteArrayInputStream("""
                GET /messages/ HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                """.getBytes(StandardCharsets.UTF_8));
        rc = new RequestContext();

        socket = mock(SocketInterface.class);

        when(socket.getInputStream()).thenReturn(request);
        when(socket.getOutputStream()).thenReturn(response);
        when(socket.getRemoteSocketAddress()).thenReturn(addr);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Test
    @DisplayName("Test get-handler (multiple messages) with mock")
    public void handleGetTest1 () throws IOException {

        Connection connection = new Connection (socket);

        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);
        String rs = response.toString();

        // Request should be successful
        assertTrue(actual);
        assertTrue(rs.contains("HTTP/1.1 200 OK"));
        // all messages should be in the response body
        assertTrue(rs.contains("ID: 1 - First") && rs.contains("ID: 2 - Second") && rs.contains("ID: 3 - Third") && rs.contains("ID: 4 - Fourth"));
    }

    @Test
    @DisplayName("Test get-handler (single existing message) with mock")
    public void handleGetTest2 () throws IOException {
        request = new ByteArrayInputStream("""
                GET /messages/2 HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                """.getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Connection connection = new Connection(socket);
        connection.getRequestHeader(rc, in, out);
        boolean actual = connection.handleRequest(rc, in, out);


        assertTrue(actual);
        assertTrue(response.toString().contains("Second"));
    }

    @Test
    @DisplayName("Test get-handler (single non-existing message and non-numeric messageID) with mock")
    public void handleGetTest3 () throws IOException {
        request = new ByteArrayInputStream("""
                GET /messages/5 HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                """.getBytes(StandardCharsets.UTF_8));

        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        rc = new RequestContext();

        Connection connection = new Connection(socket);
        connection.getRequestHeader(rc, in, out);
        boolean actual = connection.handleRequest(rc, in, out);

        assertFalse(actual);


        request = new ByteArrayInputStream("""
                GET /messages/hello HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                """.getBytes(StandardCharsets.UTF_8));

        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        rc = new RequestContext();

        connection = new Connection(socket);
        connection.getRequestHeader(rc, in, out);
        actual = connection.handleRequest(rc, in, out);

        assertFalse(actual);
    }




    @Test
    @DisplayName("Test post-handler and get-handler (correct input) with mock")
    public void handlePostTest1 () throws IOException {

        request = new ByteArrayInputStream("""
                POST /messages HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                Content-Length: 47
                
                Me feeling when cyberpunk 
                gets delayed again :(""".getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Connection connection = new Connection (socket);

        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);
        String rs = response.toString();
        System.out.println(rs);

        // Request should be successful
        assertTrue(actual);
        assertTrue(rs.contains("HTTP/1.1 201 Created"));
        assertEquals("""
                Me feeling when cyberpunk
                gets delayed again :(""", HTTPServer.getValue(5));



        // Check if the new message is also returned upon a get request


        response = new ByteArrayOutputStream();
        request = new ByteArrayInputStream("""
                GET /messages/ HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                """.getBytes(StandardCharsets.UTF_8));

        when(socket.getInputStream()).thenReturn(request);
        when(socket.getOutputStream()).thenReturn(response);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        rc = new RequestContext();

        connection = new Connection (socket);
        connection.getRequestHeader(rc, in, out);
        actual = connection.handleRequest(rc, in, out);
        rs = response.toString();

        // Request should be successful
        assertTrue(actual);
        assertTrue(rs.contains("HTTP/1.1 200 OK"));
        // all messages + the newest should be in the body
        assertTrue(
                rs.contains("ID: 1 - First") &&
                        rs.contains("ID: 2 - Second") &&
                        rs.contains("ID: 3 - Third") &&
                        rs.contains("ID: 4 - Fourth") &&
                        rs.contains("ID: 5 - Me feeling when cyberpunk\ngets delayed again :("));
    }

    @Test
    @DisplayName("Test post-handler (malformed Request header) with mock")
    public void handlePostTest2 () throws IOException {

        // /mess instead of /messages
        request = new ByteArrayInputStream("""
                POST /mess HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                Content-Length: 47
                
                Me feeling when cyberpunk 
                gets delayed again :(""".getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Connection connection = new Connection (socket);

        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);

        assertFalse(actual);
    }


    @Test
    @DisplayName("Test put-handler (correct input) with mock")
    public void handlePutTest1 () throws IOException {
        request = new ByteArrayInputStream("""
                PUT /messages/2 HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                Content-Length: 34
                
                Humanity: *exists*
                <<insert meme>>""".getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


        Connection connection = new Connection (socket);
        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);
        assertTrue(actual);
        assertEquals("Humanity: *exists*\n<<insert meme>>", HTTPServer.getValue(2));
    }

    @Test
    @DisplayName("Test put-handler (false input - message does not exist) with mock")
    public void handlePutTest2 () throws IOException {
        request = new ByteArrayInputStream("""
                PUT /messages/6 HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                Content-Length: 34
                
                Humanity: *exists*
                <<insert meme>>""".getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Connection connection = new Connection (socket);
        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);
        assertFalse(actual);
        assertTrue(response.toString().contains("Entered messageID doesn't exist."));
    }

    @Test
    @DisplayName("Test delete-handler (correct input) with mock")
    public void handleDeleteTest1 () throws IOException {
        request = new ByteArrayInputStream("""
                DELETE /messages/1 HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                Content-Length: 34
                
                Humanity: *exists*
                <<insert meme>>""".getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Connection connection = new Connection (socket);
        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);
        assertTrue(actual);
        assertNull(HTTPServer.getValue(1));
    }

    @Test
    @DisplayName("Test delete-handler (wrong input) with mock")
    public void handleDeleteTest2 () throws IOException {
        request = new ByteArrayInputStream("""
                DELETE /messages/6 HTTP/1.1
                User-Agent: DelayedRTXTestMachine3000
                Accept: */*
                Host: localhost:80
                Connection: keep-alive
                Content-Length: 34
                
                Humanity: *exists*
                <<insert meme>>""".getBytes(StandardCharsets.UTF_8));

        // request erzeugt eine neue Referenz, weswegen der Mock und der Reader angepasst werden muss
        when(socket.getInputStream()).thenReturn(request);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Connection connection = new Connection (socket);
        connection.getRequestHeader(rc, in, out);

        boolean actual = connection.handleRequest(rc, in, out);
        assertFalse(actual);
        assertTrue(response.toString().contains("Specified message doesn't exist."));
    }

}
