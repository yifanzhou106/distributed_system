package event;



import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static event.EventServer.*;

/**
 * Send HeartBeatMessage
 * A single thread handling sending HeartBeatMessage
 *
 * Primary: Keep contact with all Frontend servers and secondary servers
 * Catch Exception: Remove node from list
 *
 * Secondary: Only keep contact with Primary
 * Catch Exception: Begin Election (Bully method)
 */
public class HeartBeatMessage implements Runnable {
    private EventDataMap edm;
    private Boolean FLAG = false;

    public HeartBeatMessage(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void run() {
        String path = "/nodes";
        try {
            while (!FLAG) {
                checkAlive(edm, path);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
        }

    }

    private void checkAlive(EventDataMap edm, String path) {
        Map<String, HashMap<String, String>> nodeMap;
        if (edm.isPrimary()) {
            nodeMap = edm.getNodeMap();
            sendToReplic(nodeMap, path);
            if (DEBUG)
            System.out.println("\nTotal alive Event Server List: " + edm.getEventNodeList().toString());
            nodeMap = edm.getFrontEndMap();
            sendToReplic(nodeMap, path);
            if (DEBUG)
                System.out.println("\nTotal alive Frontend Server List: " + edm.getFrontendNodeList().toString());
        } else {
            String url = "http://" + EVENT_HOST + ":" + EVENT_PORT + path;
            try {
                sendGet(url);
            } catch (Exception e) {
                System.out.println("\nCan not connect to primary, Begin election");
                path = "/nodes/election";
                url = "http://" + HOST + ":" + PORT + path;
                try {
                    sendGet(url);
                } catch (Exception ex) {
                    System.out.println("\ncannot connect itself");
                }
            }
            if (DEBUG)
                System.out.println("\nTotal alive Event Server List: " + edm.getEventNodeList().toString());

        }
    }


    // HTTP GET request
    private int sendGet(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "HTTP/1.1");
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();
//        System.out.println("\nChecking Server: " + url);
//        System.out.println("Response Code : " + responseCode);

        return responseCode;
    }

    private void sendToReplic(Map<String, HashMap<String, String>> nodeMap, String path) {
        HashMap<String, String> singleNodeMap;
        String host, port;
        try {
            for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
                singleNodeMap = entry.getValue();
                host = singleNodeMap.get("host");
                port = singleNodeMap.get("port");

                String url = "http://" + host + ":" + port + path;
                try {
                    sendGet(url);
                } catch (Exception e) {
                    System.out.println("\nCan not connect to " + url);
                    System.out.println("\nRemoving " + url);
                    edm.removeSingleNode(host, port, nodeMap);
                    System.out.println("\nRemove successfully");
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}

