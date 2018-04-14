package event;

import frontend.BaseServlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Get event info
 */
public class EventGetterServlet extends EventBaseServlet {

    private EventDataMap edm;

    public EventGetterServlet(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            PrintWriter out = response.getWriter();
            ArrayList<String> parameterList = getUrlParameterList(request);
            String eventid = parameterList.get(3);
            printRequest(request);
            String s;
            if (eventid.equals("list")) {
                s = edm.getEventList().toString();
            } else {
                s = edm.getEventInfo(Long.parseLong(eventid)).toString();
            }
            if (s.equals("[]") || s.equals("{}"))
                response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

            response.setContentType("application/json");
            out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        response.setStatus(400);

    }

}
