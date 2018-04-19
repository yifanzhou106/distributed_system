package event.replication;

import event.EventBaseServlet;
import event.EventDataMap;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


public class AddFrontEndNodeServlet extends EventBaseServlet {
    private EventDataMap edm;
    private Map<String, HashMap<String, String>> nodeMap;

    public AddFrontEndNodeServlet(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    }
    /**
     * Add a Single Frontend Server
     * If Primary, then do send replics
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String body = extractPostRequestBody(request);
        String HOST, PORT;
        try {
            JSONObject jsonobj = readJsonObj(body);
            JSONObject item = (JSONObject) jsonobj.get("frontend");
            HOST = (String) item.get("host");
            PORT = (String) item.get("port");
            System.out.println("\nAdd a new front end " + HOST + PORT);
            edm.addSingleFrontendNode(HOST, PORT);
            System.out.println("\nAdd a new front end successfully");
            System.out.println("Total alive Front End Server List: " + edm.getFrontendNodeList().toString());
            if (edm.isPrimary()) {
                String path = "/nodes/add/frontend";
                System.out.println("\nSending new frontend to replic");
                nodeMap = edm.getNodeMap();
                sendToReplic(response, nodeMap, body, path);
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
