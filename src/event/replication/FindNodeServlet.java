package event.replication;

import event.EventBaseServlet;
import event.EventDataMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Create events
 */
public class FindNodeServlet extends EventBaseServlet {
    private EventDataMap edm;

    public FindNodeServlet(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        PrintWriter out = response.getWriter();

        String s;
        s = edm.getNodeList();
        response.setContentType("application/json");
        out.println(s);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        PrintWriter out = response.getWriter();
        String s = extractPostRequestBody(request);
        String HOST, PORT;
        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(s);
            JSONObject obj = (JSONObject) jsonObj;
            JSONObject item = (JSONObject) obj.get("follower");
            HOST = (String) item.get("host");
            PORT = (String) item.get("port");
            edm.addSingleNode(HOST, PORT);

            if (edm.isPrimary()) {
                String path = "/nodes";
                sendToReplic(response, edm, s, path);
                String responseS;
                responseS = edm.getNodeList();
                response.setContentType("application/json");
                out.println(responseS);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
