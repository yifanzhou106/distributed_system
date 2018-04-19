package frontend;


import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static frontend.FrontEndServer.*;

/**
 * Create event, send info to event server
 */
public class CreateEventServlet extends BaseServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(400);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();

        try {
            String body = extractPostRequestBody(request);
            JSONObject obj = readJsonObj(body);
            long userid = (Long) obj.get("userid");
            String eventname = (String) obj.get("eventname");
            long numtickets = (Long) obj.get("numtickets");
            String timestamp = getTimeStamp(HOST, PORT);

            JSONObject json = new JSONObject();
            json.put("userid", userid);
            json.put("eventname", eventname);
            json.put("numtickets", numtickets);
            json.put("timestamp", timestamp);
            String url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/create";
            String responseS;
            try {
                responseS = sendPost(response, url, json.toString());
            } catch (Exception e) {
                System.out.println("\nPrimary Event server error, resend in one second");
                Thread.sleep(1000);
                responseS = sendPost(response, url, json.toString());
            }


            out.println(responseS);
        } catch (Exception e) {
            response.setStatus(400);
            e.printStackTrace();
        }
    }
}
