package frontend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static frontend.FrontEndServer.EVENT_HOST;
import static frontend.FrontEndServer.EVENT_PORT;

/**
 * Create event, send info to event server
 */
public class OtherServlet extends BaseServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        response.setStatus(400);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        response.setStatus(400);

    }
}
