package event;


import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.*;

import static event.EventServer.PORT;

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
        response.setStatus(400);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            PrintWriter out = response.getWriter();
            String body = extractPostRequestBody(request);
            System.out.println("json: "+body);
            long eventid;
            long userid;
            String eventname;
            long numtickets;
            String timestamp;

            userid = (Long) readJsonObj(body, "userid");
            eventname = (String) (readJsonObj(body, "eventname"));
            numtickets = (Long) (readJsonObj(body, "numtickets"));
            timestamp = (String) (readJsonObj(body, "timestamp"));

            if (!edm.isTimeStampExist(timestamp)) {
                edm.addTimeStamp(timestamp);

                JSONObject json = new JSONObject();
                if (edm.isPrimary()) {
                    eventid = edm.createRandomEventId();
                    System.out.println(eventid);
                    json.put("eventid", eventid);
                    json.put("userid", userid);
                    json.put("eventname", eventname);
                    json.put("numtickets", numtickets);
                    json.put("timestamp", timestamp);
                    String path = "/create";
                    sendToReplic(response, edm, json.toString(), path);

                    response.setContentType("application/json");
                    json = new JSONObject();
                    json.put("eventid", eventid);
                    json.put("timestamp", timestamp);
                    body = json.toString();
                } else {
                    eventid = (Long) readJsonObj(body, "eventid");
                }
                System.out.println("Create a new event: "+ body);
                edm.createNewEvent(eventid, eventname, userid, numtickets, 0);
                out.println(body);
            }
            else
            {
                System.out.println("\nRepeat Time Stamp "+ body);
            }


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }
}
