package event.replication;

import event.EventBaseServlet;
import org.json.simple.JSONObject;

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
    private NodeMap nm;

    public FindNodeServlet(NodeMap nm) {
        this.nm = nm;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        PrintWriter out = response.getWriter();

        String s;
        s = nm.getNodeList();
        if (s.equals("[]"))
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        response.setContentType("application/json");
        out.println(s);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String HOST, PORT;
        Map<String, HashMap<String, String>> nodeMap;
        HashMap<String, String> singleNodeMap;
        nodeMap = nm.getNodeMap();
        try {
            String s = extractPostRequestBody(request);
            for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
                singleNodeMap = entry.getValue();
                HOST = singleNodeMap.get("host");
                PORT = singleNodeMap.get("port");
                String url = "http://" + HOST + ":" + PORT + "/node/add";
                int responseCode;
                responseCode = sendReplicationPost(url, s);
                if (responseCode == 400)
                    response.setStatus(400);

                System.out.println(responseCode);
            }
        } catch (Exception e) {
            response.setStatus(400);
            e.printStackTrace();
        }
    }
}
