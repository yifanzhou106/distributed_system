package event.replication;

import event.EventBaseServlet;
import event.EventDataMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Create events
 */
public class AddFrontEndNodeServlet extends EventBaseServlet {
    private EventDataMap edm;

    public AddFrontEndNodeServlet(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String s = extractPostRequestBody(request);
        String HOST, PORT;
        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(s);
            JSONObject obj = (JSONObject) jsonObj;
            JSONObject item = (JSONObject) obj.get("frontend");
            HOST = (String) item.get("host");
            PORT = (String) item.get("port");
            System.out.println("\nAdd a new front end " + HOST + PORT);
            edm.addSingleFrontendNode(HOST, PORT);
            System.out.println("\nAdd a new front end successfully");
            System.out.println("Total alive Front End Server List: " + edm.getFrontendNodeList().toString());
            if (edm.isPrimary()) {
                String path = "/nodes/add/frontend";
                System.out.println("\nSending new frontend to replic");
                sendToReplic(response, edm, s, path);
                String responseS;
                responseS = edm.getNodeList();
                response.setContentType("application/json");
                out.println(responseS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
