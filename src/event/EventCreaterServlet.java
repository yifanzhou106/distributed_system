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
    private QueueWorker qw;


    public EventCreaterServlet(EventDataMap edm,QueueWorker qw) {
        this.edm = edm;
        this.qw = qw;
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
            long eventid;
            long userid;
            String eventname;
            long numtickets;
            String timestamp;
            String VersionID;

            userid = (Long) readJsonObj(body, "userid");
            eventname = (String) (readJsonObj(body, "eventname"));
            numtickets = (Long) (readJsonObj(body, "numtickets"));
            timestamp = (String) (readJsonObj(body, "timestamp"));

            if (!edm.isTimeStampExist(timestamp)) {
                edm.addTimeStamp(timestamp);

                JSONObject json = new JSONObject();
                if (edm.isPrimary()) {
                    edm.increaseVid();
                    VersionID = String.valueOf( edm.getVersionID()) ;
                    eventid = edm.createRandomEventId();
                    json.put("eventid", eventid);
                    json.put("userid", userid);
                    json.put("eventname", eventname);
                    json.put("numtickets", numtickets);
                    json.put("timestamp", timestamp);
                    json.put("vid",VersionID);
                    String path = "/create";
                    sendToReplic(response, edm, json.toString(), path);

                    response.setContentType("application/json");
                    json = new JSONObject();
                    json.put("eventid", eventid);
                    json.put("timestamp", timestamp);
                    body = json.toString();
                    edm.createNewEvent(eventid, eventname, userid, numtickets, 0);

                } else {
                    QueueObject obj =new QueueObject("create","post",body);
                    qw.enqueue(obj);
                }
//                System.out.println("Create a new event: " + body);
                out.println(body);
            } else {
                System.out.println("\nRepeat Time Stamp " + body);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }
}
