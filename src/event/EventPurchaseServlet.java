package event;

import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static event.EventServer.*;

/**
 * Purchase event tickets
 */
public class EventPurchaseServlet extends EventBaseServlet {
    private EventDataMap edm;
    private QueueWorker qw;
    private Map<String, HashMap<String, String>> nodeMap;


    public EventPurchaseServlet(EventDataMap edm, QueueWorker qw) {
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
            JSONObject jsonobj = readJsonObj(body);

            long eventid;
            long userid;
            long tickets;
            String timestamp;
            String VersionID;
            String s;
            eventid = (Long) jsonobj.get("eventid");
            userid = (Long) jsonobj.get("userid");
            tickets = (Long) jsonobj.get("tickets");
            timestamp = (String) jsonobj.get("timestamp");
            Boolean isSuccess = false;

            if (edm.isPrimary()) {
                String responseS;
                if (!edm.isTimeStampExist(timestamp)) {
                    edm.addTimeStamp(timestamp, eventid);
                    isSuccess = edm.purchaseTicket(eventid, tickets);
                }
                VersionID = String.valueOf(edm.getVersionIDIncreased());
                if (DEBUG) {
                    if (VersionID.equals(DEBUG_NUM) && (PORT == 5650)) {
                        System.out.println("Debug Mode");
                        System.out.println(PORT);
                        System.exit(-1);
                    }
                }
                String path = "/purchase/" + eventid;
                JSONObject json = new JSONObject();
                json.put("eventid", eventid);
                json.put("userid", userid);
                json.put("tickets", tickets);
                json.put("timestamp", timestamp);
                json.put("vid", VersionID);
                nodeMap = edm.getNodeMap();
//                    threads.submit(new sendToReplic(response, nodeMap, json.toString(), path));
                String key = HOST + PORT;

                sendToReplic(response, nodeMap, json.toString(), path, key);

                if (isSuccess) {
                    String url = "http://" + USER_HOST + ":" + USER_PORT + "/" + userid + "/tickets/add";  //Change to user server
                    response.setContentType("application/json");
                    json = new JSONObject();
                    json.put("eventid", eventid);
                    json.put("tickets", tickets);
                    s = json.toString();
                    System.out.println(s);
                    responseS = sendPost(response, url, s);
                    out.println(responseS);

                    System.out.println("Purchase Successfully\n");

                } else {
                    System.out.println("Purchase Unsuccessfully\n");
                    response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
                }


            } else {
                QueueObject obj = new QueueObject("purchase", "post", body);
                qw.enqueue(obj);
                while (obj.getFinishFlag()) {
                }
            }


        } catch (Exception e) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            e.printStackTrace();
        }

    }
}
