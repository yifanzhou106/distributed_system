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


public class FindNodeServlet extends EventBaseServlet {
    private EventDataMap edm;
    private Map<String, HashMap<String, String>> nodeMap;

    public FindNodeServlet(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    }

    /**
     * Add a Single Secondary Server
     * If Primary, then do send replics
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String s = extractPostRequestBody(request);
        System.out.println("\nNew Event Node is "+s);
        String HOST, PORT;
        try {
            JSONObject jsonobj = readJsonObj(s);
            JSONObject item = (JSONObject) jsonobj.get("follower");
            HOST = (String) item.get("host");
            PORT = (String) item.get("port");
            edm.addSingleNode(HOST, PORT);
            System.out.println("\nAdd Event node successfully "+s);

            if (edm.isPrimary()) {
                String path = "/nodes";
                nodeMap = edm.getNodeMap();
                sendToReplic(response, nodeMap, s, path);
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
