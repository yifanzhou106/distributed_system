package event;

import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static event.EventServer.USER_HOST;
import static event.EventServer.USER_PORT;

/**
 * Purchase event tickets
 */
public class EventPurchaseServlet extends EventBaseServlet {
    private EventDataMap edm;
    private QueueWorker qw;


    public EventPurchaseServlet(EventDataMap edm, QueueWorker qw) {
        this.edm = edm;
        this.qw = qw;
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
            long tickets;
            String timestamp;
            String VersionID;

            String s;
            eventid = (Long) (readJsonObj(body, "eventid"));
            userid = (Long) (readJsonObj(body, "userid"));
            tickets = (Long) (readJsonObj(body, "tickets"));
            timestamp = (String) (readJsonObj(body, "timestamp"));

            if (!edm.isTimeStampExist(timestamp)) {
                edm.addTimeStamp(timestamp);
                Boolean isSuccess = edm.purchaseTicket(eventid, tickets);
                edm.increaseVid();
                VersionID = String.valueOf(edm.getVersionID());
                if (edm.isPrimary()) {
                    String path = "/purchase/" + eventid;
                    if (isSuccess) {
                        JSONObject json = new JSONObject();
                        json.put("eventid", eventid);
                        json.put("tickets", tickets);
                        json.put("vid", VersionID);
                        sendToReplic(response, edm, json.toString(), path);

                        String url = "http://" + USER_HOST + ":" + USER_PORT + "/" + userid + "/tickets/add";  //Change to user server
                        response.setContentType("application/json");
                        json = new JSONObject();
                        json.put("eventid", eventid);
                        json.put("tickets", tickets);
                        s = json.toString();
                        System.out.println(s);
                        String responseS;
//                        responseS = sendPost(response, url, s);
//                        out.println(responseS);
                        System.out.println("Purchase Successfully\n");

                    } else {
                        System.out.println("Purchase Unsuccessfully\n");
                        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                } else {
                    QueueObject obj = new QueueObject("purchase", "post", body);
                    qw.enqueue(obj);
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            System.out.println(e);
        }

    }
}
