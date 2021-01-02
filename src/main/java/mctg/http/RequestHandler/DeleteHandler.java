package mctg.http.RequestHandler;

import mctg.http.HTTPServer;
import mctg.http.RequestContext;

import java.io.BufferedWriter;

public class DeleteHandler extends Thread {


    private final RequestContext rc;
    private final BufferedWriter out;
    private boolean result = false;

    public DeleteHandler (RequestContext rc, BufferedWriter out) {
        this.rc = rc;
        this.out = out;
    }

    @Override
    public void run() {
        String[] parts = rc.getValue("url").split("/");
        if (parts.length < 3 || rc.getValue("Authorization") == null) {
            RequestContext.writeToSocket(400, "Malformed url" + System.lineSeparator(), out);
            result = false;
            return;
        }

        if (parts[1].equalsIgnoreCase("tradings")) {
            rc.setValue("Authorization", rc.getValue("Authorization").substring("Basic ".length()));

            if (HTTPServer.db.deleteTradingOffer(rc.getValue("Authorization"), parts[2])) {
                RequestContext.writeToSocket(200, "Successfully deleted trading offer" + System.lineSeparator(), out);
                result = true;
                return;
            } else {
                RequestContext.writeToSocket(400, "An error occurred while trying to delete offer. Please verify that token is valid", out);
            }
        }

        result = false;
    }

    public boolean getResult() {
        return result;
    }
}
