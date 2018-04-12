package frontend;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FrontEndMap {

    private ReentrantReadWriteLock rwl;

    private String PrimaryHost;
    private String PrimaryPort;

    private Map<String, HashMap<String, String>> frontEndMap;
    private HashMap<String, String> singleNodeMap;

    private long timestamp;

    public FrontEndMap() {
        frontEndMap = new TreeMap<>();
        rwl = new ReentrantReadWriteLock();
        timestamp = 0;
    }


    /**
     * To string
     *
     * @param eventid
     * @param eventname
     * @param userid
     * @param avail
     * @param purchased
     * @return
     */
    public JSONObject toJsonObject(long eventid, String eventname, long userid, long avail, long purchased) {
        JSONObject json = new JSONObject();
        json.put("eventid", eventid);
        json.put("eventname", eventname);
        json.put("userid", userid);
        json.put("avail", avail);
        json.put("purchased", purchased);

        return json;
    }


    public Long getTimeStamp() {
        rwl.writeLock().lock();
        timestamp++;
        rwl.writeLock().unlock();

        return timestamp;
    }


    public void addNode(String jsonString) {
        String host, port, key;

        try {
            rwl.writeLock().lock();
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(jsonString);
            JSONObject JsonObj = (JSONObject) jsonObj;
            JSONArray frontEndNode = (JSONArray) JsonObj.get("frontendnode");
            timestamp = (Long) JsonObj.get("timestamp");
            PrimaryHost = (String)JsonObj.get("host");
            PrimaryPort = (String)JsonObj.get("port");


            for (int i = 0; i < frontEndNode.size(); i++) {
                JSONObject item = (JSONObject) frontEndNode.get(i);
                host = (String) item.get("host");
                port = (String) item.get("port");
                key = host + port;
                if (!frontEndMap.containsKey(key)) {
                    singleNodeMap = new HashMap();
                    singleNodeMap.put("host", host);
                    singleNodeMap.put("port", port);
                    frontEndMap.put(key, singleNodeMap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public String getNodeList() {
        rwl.readLock().lock();
        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();
        for (Map.Entry<String, HashMap<String, String>> entry : frontEndMap.entrySet()) {
            String key = entry.getKey();
            item = getNodeInfo(key);
            jsonArray.add(item);
        }
        rwl.readLock().unlock();

        return jsonArray.toString();
    }

    public JSONObject getNodeInfo(String key) {
        JSONObject s = new JSONObject();
        rwl.readLock().lock();
        if (frontEndMap.containsKey(key)) {
            String host, port;
            singleNodeMap = frontEndMap.get(key);
            host = singleNodeMap.get("host");
            port = singleNodeMap.get("port");

            s = toJsonObject(host, port);
        }
        rwl.readLock().unlock();
        return s;
    }

    public JSONObject toJsonObject(String host, String port) {
        JSONObject json = new JSONObject();
        json.put("host", host);
        json.put("port", port);
        return json;
    }
}
