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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static event.EventServer.*;

/**
 * Create events
 */
public class EventCreaterServlet extends EventBaseServlet {
    private EventDataMap edm;
    private QueueWorker qw;
    private Map<String, HashMap<String, String>> nodeMap;
    private ExecutorService threads;


    public EventCreaterServlet(EventDataMap edm, QueueWorker qw) {
        this.edm = edm;
        this.qw = qw;
        threads = Executors.newCachedThreadPool();
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
            JSONObject jsonobj = readJsonObj(body);

            long eventid;
            long userid;
            String eventname;
            long numtickets;
            String timestamp;
            String VersionID;
            String s;
            userid = (Long) jsonobj.get("userid");
            eventname = (String) jsonobj.get("eventname");
            numtickets = (Long) jsonobj.get("numtickets");
            timestamp = (String) jsonobj.get("timestamp");

            JSONObject json = new JSONObject();
            if (edm.isPrimary()) {
                VersionID = String.valueOf(edm.getVersionIDIncreased());
                if (DEBUG) {
                    if (VersionID.equals(DEBUG_NUM) && (PORT == 5650)) {
                        System.out.println("Debug Mode");
                        System.out.println(PORT);
                        System.exit(-1);
                    }
                }
                if (!edm.isTimeStampExist(timestamp)) {
                    eventid = edm.createRandomEventId();
                    edm.addTimeStamp(timestamp, eventid);
                    edm.createNewEvent(eventid, eventname, userid, numtickets, 0);
                    System.out.println("\nCreate Successfully");
                } else {
                    System.out.println("\nRepeat Time Stamp " + body);
                    eventid = edm.getEventidFromTimestamp(timestamp);

                }
                json.put("eventid", eventid);
                json.put("userid", userid);
                json.put("eventname", eventname);
                json.put("numtickets", numtickets);
                json.put("timestamp", timestamp);
                json.put("vid", VersionID);
                s = json.toString();
                String path = "/create";
                nodeMap = edm.getNodeMap();
                String key = HOST + PORT;

                sendToReplic(response, nodeMap, s, path, key);
//                threads.submit(new sendToReplic(response, nodeMap, json.toString(), path, key));

                response.setContentType("application/json");
                json = new JSONObject();
                json.put("eventid", eventid);
                body = json.toString();

            } else {
                QueueObject obj = new QueueObject("create", "post", body);
                qw.enqueue(obj);
                while (obj.getFinishFlag()) {
                }
            }
            out.println(body);


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

    }

}
