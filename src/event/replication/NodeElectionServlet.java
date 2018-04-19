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

public class NodeElectionServlet extends EventBaseServlet {
    private EventDataMap edm;
    private Map<String, HashMap<String, String>> nodeMap;


    public NodeElectionServlet(EventDataMap edm) {
        this.edm = edm;
    }

    /**
     * Election method
     * Send GET method to every node (Key) greater than itself
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        out.println();
        String host, port;
        try {
            host = edm.getFollowerHost();
            port = edm.getFollowerPort();

            Map<String, HashMap<String, String>> nodeMap;
            HashMap<String, String> singleNodeMap;
            nodeMap = edm.getNodeMap();
            String followerHost, followerPort;
            String key = host + port;
            String path = "/nodes/election";
            Boolean canBePrimary = true;
            for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
                if (key.compareToIgnoreCase(entry.getKey()) < 0) {
                    singleNodeMap = entry.getValue();
                    followerHost = singleNodeMap.get("host");
                    followerPort = singleNodeMap.get("port");
                    String url = "http://" + followerHost + ":" + followerPort + path;
                    try {
                        if (sendGet(url) == 200)
                            canBePrimary = false;
                    } catch (Exception e) {
                        System.out.println("\nCan not connect to " + url);
                    }
                }
            }
            if (canBePrimary) {
                edm.setPrimaryHost(host);
                edm.setPrimaryPort(port);
                String s = edm.getPrimaryJsonString();
                nodeMap =edm.getNodeMap();
                sendToReplic(response, nodeMap, s, path);
                path = "/nodes";
                nodeMap =edm.getFrontEndMap();
                sendToReplic(response, nodeMap, s, path);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Election Finished
     * Receive new Primary server's POST, and update Primary Info locally
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
            JSONObject item = (JSONObject) jsonobj.get("primary");
            HOST = (String) item.get("host");
            PORT = (String) item.get("port");
            System.out.println("\nElection Finished");
            System.out.println("\nNow primary is "+HOST+PORT);
            edm.setPrimaryHost(HOST);
            edm.setPrimaryPort(PORT);
            out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
