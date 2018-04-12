package event;


import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;

import static event.EventServer.PORT;

/**
 * Create events
 */
public class EventAddServlet extends EventBaseServlet {
    private EventDataMap edm;

    public EventAddServlet(EventDataMap edm) {
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
            String body = extractPostRequestBody(request);

            long eventid;
            long userid;
            String eventname;
            long numtickets;

            eventid=(Long) readJsonObj(body, "eventid");
            userid = (Long) readJsonObj(body, "userid");
            eventname = (String) (readJsonObj(body, "eventname"));
            numtickets = (Long) (readJsonObj(body, "numtickets"));
            edm.createNewEvent(eventid, eventname, userid, numtickets, 0);


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }
}
