package event;

import frontend.BaseServlet;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Create events
 */
public class EventCreaterServlet extends EventBaseServlet {
    private EventDataMap edm;

    public EventCreaterServlet(EventDataMap edm) {
        this.edm = edm;
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        response.setStatus(400);

    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            printRequest(request);
            PrintWriter out = response.getWriter();
            String body = extractPostRequestBody(request);
            long eventid;
            long userid;
            String eventname;
            long numtickets;

            String s;
            userid = (Long) readJsonObj(body, "userid");
            eventname = (String) (readJsonObj(body, "eventname"));
            numtickets = (Long) (readJsonObj(body, "numtickets"));
            eventid = edm.createRandomEventId();
            System.out.println(eventid);
            edm.createNewEvent(eventid, eventname, userid, numtickets, 0);
            response.setContentType("application/json");
            JSONObject json = new JSONObject();
            json.put("eventid",eventid);
            s = json.toString();
            out.println(s);


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }
}
