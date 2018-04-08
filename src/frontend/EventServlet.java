package frontend;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static frontend.FrontEndServer.EVENT_HOST;
import static frontend.FrontEndServer.EVENT_PORT;

/**
 * Handle with Events
 */
public class EventServlet extends BaseServlet {
    /**
     * GET /events/{eventid}
     * GET /events
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        printRequest(request);
        PrintWriter out = response.getWriter();
        String url;
        try {
            ArrayList<String> parameterList = getUrlParameterList(request);
            String eventid;
            if (!parameterList.isEmpty()) {
                eventid = parameterList.get(0);
                url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/" + eventid;
                System.out.println(url);

            } else {
                url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/list";
                System.out.println(url);
            }
            String responseS;
            responseS = sendGet(response, url);
            out.println(responseS);
        } catch (Exception e) {
            response.setStatus(400);
            System.out.println(e);
        }

    }

    /**
     * POST /events/{eventid}/purchase/{userid}
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String url;
        String body = extractPostRequestBody(request);
        printRequest(request);

        try {
            ArrayList<String> parameterList = getUrlParameterList(request);
            String eventid;
            String userid;
            String s;
            if (parameterList.size() == 2) {
                eventid = parameterList.get(0);
                userid = parameterList.get(1);

                long tickets = (Long) readJsonObj(body, "tickets");
                JSONObject json = new JSONObject();
                json.put("userid", Long.parseLong(userid));
                json.put("eventid", Long.parseLong(eventid));
                json.put("tickets", tickets);

                s = json.toString();
                url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/purchase/" + eventid;

                String responseS;
                responseS = sendPost(response, url, s);
                out.println(responseS);
            } else {
                response.setStatus(400);
            }
        } catch (Exception e) {
            response.setStatus(400);
            System.out.println(e);
        }

    }
}
