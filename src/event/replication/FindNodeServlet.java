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
import static event.EventServer.*;


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
        String host, port;
        try {
            JSONObject jsonobj = readJsonObj(s);
            JSONObject item = (JSONObject) jsonobj.get("follower");
            host = (String) item.get("host");
            port = (String) item.get("port");
            edm.addSingleNode(host, port);
            System.out.println("\nAdd Event node successfully "+s);
            System.out.println("\nTotal alive Event Server List: " + edm.getEventNodeList().toString());

            if (edm.isPrimary()) {
                String path = "/nodes";
                nodeMap = edm.getNodeMap();
                String key = HOST + PORT;
                sendToReplic(response, nodeMap, s, path,key);
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
