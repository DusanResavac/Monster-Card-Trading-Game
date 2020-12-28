package mctg.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public interface ConnectionInterface {
    boolean handleRequest (RequestContext rc, BufferedReader in, BufferedWriter out);

    boolean handleGet (RequestContext rc, BufferedWriter out);
    boolean handlePost (RequestContext rc, BufferedReader in, BufferedWriter out);
    boolean handlePut (RequestContext rc, BufferedReader in, BufferedWriter out);
    boolean handleDelete (RequestContext rc, BufferedWriter out);
}
