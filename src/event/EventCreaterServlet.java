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

            String HOST, PORT;
            Map<String, HashMap<String, String>> nodeMap;
            HashMap<String, String> singleNodeMap;
            nodeMap = edm.getNodeMap();

            long eventid;
            long userid;
            String eventname;
            long numtickets;
            long timestamp;

            String s;
            userid = (Long) readJsonObj(body, "userid");
            eventname = (String) (readJsonObj(body, "eventname"));
            numtickets = (Long) (readJsonObj(body, "numtickets"));
            eventid = edm.createRandomEventId();
            System.out.println(eventid);

            JSONObject json = new JSONObject();
            json.put("eventid",eventid);
            json.put("userid",userid);
            json.put("eventname",eventname);
            json.put("numtickets",numtickets);
            edm.createNewEvent(eventid, eventname, userid, numtickets, 0);

            timestamp = edm.getTimeStamp();
            json = new JSONObject();
            json.put("eventid",eventid);

            for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
                singleNodeMap = entry.getValue();
                HOST = singleNodeMap.get("host");
                PORT = singleNodeMap.get("port");
                String url = "http://" + HOST + ":" + PORT + "/node/add";
                sendReplicationPost(url, json.toString());
            }

            response.setContentType("application/json");
             json = new JSONObject();
            json.put("eventid",eventid);
            s = json.toString();
            out.println(s);


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }
}
