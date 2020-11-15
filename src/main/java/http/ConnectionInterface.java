package http;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public interface ConnectionInterface {
    boolean handleRequest (RequestContext rc, BufferedReader in, BufferedWriter out);

    boolean handleGet (String[] parts, BufferedWriter out);
    boolean handlePost (RequestContext rc, BufferedReader in, BufferedWriter out);
    boolean handlePut (String[] parts, RequestContext rc, BufferedReader in, BufferedWriter out);
    boolean handleDelete (String[] parts, BufferedWriter out);
}
