package event;


import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Get into receive message threads
 * Will create a socket to receive messages
 */
public class HeartBeatMessage implements Runnable {
    private EventDataMap edm;
    Boolean FLAG = false;

    public HeartBeatMessage(EventDataMap edm) {
        this.edm = edm;
    }

    @Override
    public void run() {
        String path = "/nodes";
        try {
            while (!FLAG) {
                sendToReplic(edm, path);
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        return responseCode;
    }

    private void sendToReplic(EventDataMap edm, String path) {
        Map<String, HashMap<String, String>> nodeMap;
        HashMap<String, String> singleNodeMap;
        String host, port;
        if (edm.isPrimary()) {
            nodeMap = edm.getNodeMap();
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
                        edm.removeSingleNode(host, port);
                        System.out.println("\nRemove successfully");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            host = edm.getPrimaryHost();
            port = edm.getPrimaryPort();
            String url = "http://" + host + ":" + port + path;
            try {
                sendGet(url);
            } catch (Exception e) {
                System.out.println("\nCan not connect to primary,Begin election");
                String followerHost, followerPort;
                followerHost = edm.getFollowerHost();
                followerPort = edm.getFollowerPort();
                beginElection(followerHost,followerPort);

            }
        }
    }


    private void beginElection(String host, String port) {
        Map<String, HashMap<String, String>> nodeMap;
        HashMap<String, String> singleNodeMap;
        nodeMap = edm.getNodeMap();
        String followerHost, followerPort;
        String key = host + port;
        String path = "/nodes/election";
        Boolean canBePrimary = true;
        for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
            if (key.compareToIgnoreCase(entry.getKey())<0) {
                singleNodeMap = entry.getValue();
                followerHost = singleNodeMap.get("host");
                followerPort = singleNodeMap.get("port");
                String url = "http://" + followerHost + ":" + followerPort + path;
                try {
                   if (sendGet(url)==200)
                       canBePrimary=false;
                }catch(Exception e)
                {
                    System.out.println("\nCan not connect to " + url);
                }
            }
        }
        if (canBePrimary)
        {

        }
    }

}

