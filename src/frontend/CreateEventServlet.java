package frontend;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static frontend.FrontEndServer.EVENT_HOST;
import static frontend.FrontEndServer.EVENT_PORT;

/**
 * Create event, send info to event server
 */
public class CreateEventServlet extends BaseServlet {
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
        PrintWriter out = response.getWriter();

        try {
            String body = extractPostRequestBody(request);
            long userid = (Long) readJsonObj(body, "userid");
            String eventname = (String) (readJsonObj(body, "eventname"));
            long numtickets = (Long) (readJsonObj(body, "numtickets"));
            String url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/create";
            String responseS;
            responseS = sendPost(response, url, body);
            out.println(responseS);
        } catch (Exception e) {
            response.setStatus(400);
            e.printStackTrace();
        }
    }
}
