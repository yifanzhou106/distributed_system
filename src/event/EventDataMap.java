package event;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handle with all Event info in a map
 */
public class EventDataMap {
    private Map<Long, HashMap<String, Object>> eventMap;
    private HashMap<String, Object> singleEventMap;
    private ReentrantReadWriteLock rwl;
    private ReentrantReadWriteLock nodelock;
    private ReentrantReadWriteLock timestamplock;


    private Map<String, HashMap<String, String>> nodeMap;
    private Map<String, HashMap<String, String>> frontEndMap;
    private HashMap<String, String> singleNodeMap;

    private HashSet<String> timeStampsSet;
    private String PrimaryHost;
    private String PrimaryPort;

    private String FollowerHost;
    private String FollowerPort;

    public EventDataMap() {
        eventMap = new TreeMap<>();
        nodeMap = new TreeMap<>();
        frontEndMap = new TreeMap<>();
        rwl = new ReentrantReadWriteLock();
        nodelock = new ReentrantReadWriteLock();
        timestamplock = new ReentrantReadWriteLock();
        timeStampsSet = new HashSet<>();
        PrimaryHost = "localhost";
        PrimaryPort = "7000";

        FollowerHost = "localhost";
        FollowerPort = "7050";
    }


    /**
     * Create a random event id
     *
     * @return
     */
    public Long createRandomEventId() {
        Random rand = new Random();
        long id;
        rwl.readLock().lock();
        do {
            id = rand.nextInt(10000);
        } while (eventMap.containsKey(id));
        rwl.readLock().unlock();

        return id;
    }

    /**
     * Create new event and store it in to map
     *
     * @param eventid
     * @param eventname
     * @param userid
     * @param avail
     * @param purchased
     */
    public void createNewEvent(long eventid, String eventname, long userid, long avail, long purchased) {
        System.out.println("createNewEvent");

        rwl.writeLock().lock();
        singleEventMap = new HashMap();
        singleEventMap.put("eventname", eventname);
        singleEventMap.put("userid", userid);
        singleEventMap.put("avail", avail);
        singleEventMap.put("purchased", purchased);
        eventMap.put(eventid, singleEventMap);
        rwl.writeLock().unlock();
    }

    /**
     * Get event list
     *
     * @return
     */
    public String getEventList() {
        rwl.readLock().lock();
        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();
        for (Map.Entry<Long, HashMap<String, Object>> entry : eventMap.entrySet()) {
            long eventid = entry.getKey();
            item = getEventInfo(eventid);
            jsonArray.add(item);
        }
        rwl.readLock().unlock();

        return jsonArray.toString();
    }

    /**
     * Get single event info
     *
     * @param eventid
     * @return
     */
    public JSONObject getEventInfo(Long eventid) {
        JSONObject s = new JSONObject();
        rwl.readLock().lock();
        if (eventMap.containsKey(eventid)) {
            String eventname;
            long userid, avail, purchased;
            singleEventMap = eventMap.get(eventid);
            eventname = (String) singleEventMap.get("eventname");
            userid = (Long) singleEventMap.get("userid");
            avail = (Long) singleEventMap.get("avail");
            purchased = (Long) singleEventMap.get("purchased");
            s = toJsonObject(eventid, eventname, userid, avail, purchased);
        }
        rwl.readLock().unlock();
        return s;
    }

    /**
     * Purchase tickets and do post to user
     *
     * @param eventid
     * @param tickets
     * @return
     */
    public Boolean purchaseTicket(long eventid, long tickets) {
        Boolean isSuccess = false;
        System.out.println("In purchase Tacket");

        rwl.writeLock().lock();

        if (eventMap.containsKey(eventid)) {
            System.out.println("In purchase Tacket");
            String eventname;
            long userid, avail, purchased;
            singleEventMap = eventMap.get(eventid);
            eventname = (String) singleEventMap.get("eventname");
            userid = (Long) singleEventMap.get("userid");
            avail = (Long) (singleEventMap.get("avail"));
            purchased = (Long) (singleEventMap.get("purchased"));
            System.out.println(eventname + userid + avail + purchased);
            if (avail - tickets >= 0) {
                avail = avail - tickets;
                purchased = purchased + tickets;
                isSuccess = true;
                eventMap.remove(eventid);
                System.out.println("Create Event\n");

                createNewEvent(eventid, eventname, userid, avail, purchased);
                System.out.println("Create Event finished\n");
            } else {
                System.out.println("Neg cases: " + ((Long) (singleEventMap.get("avail")) - tickets));
            }
        }
        rwl.writeLock().unlock();


        return isSuccess;
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

    public Map<String, HashMap<String, String>> getNodeMap() {
        return nodeMap;
    }

    public Boolean isPrimary() {
        return PrimaryHost.equals(FollowerHost) && PrimaryPort.equals(FollowerPort);
    }

    public Boolean isTimeStampExist(String timestamp) {
        Boolean is_exist = false;
        nodelock.readLock().lock();
        if (timeStampsSet.contains(timestamp))
            is_exist = true;
        nodelock.readLock().unlock();

        return is_exist;
    }

    public void addTimeStamp(String timestamp) {
        timestamplock.writeLock().lock();
        timeStampsSet.add(timestamp);
        timestamplock.writeLock().unlock();

    }

    public void addSingleNode(String host, String port) {
        nodelock.writeLock().lock();
        singleEventMap = new HashMap();
        String key = host + port;
        singleNodeMap.put("host", host);
        singleNodeMap.put("port", port);
        nodeMap.put(key, singleNodeMap);
        nodelock.writeLock().unlock();

    }

    public void removeSingleNode (String host, String port){
        nodelock.writeLock().lock();
        String key = host + port;
        nodeMap.remove(key);
        nodelock.writeLock().unlock();

    }

    public void addNode(String jsonString) {
        String host, port, key;

        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(jsonString);
            JSONObject JsonObj = (JSONObject) jsonObj;

            JSONObject primary = (JSONObject) JsonObj.get("primary");
            JSONArray eventNode = (JSONArray) JsonObj.get("eventnode");
            JSONArray frontEndNode = (JSONArray) JsonObj.get("frontendnode");
            JSONArray eventMap = (JSONArray) JsonObj.get("eventmap");
            JSONArray timestamps = (JSONArray) JsonObj.get("timestamps");

            PrimaryHost = (String) primary.get("host");
            PrimaryPort = (String) primary.get("port");

            nodelock.writeLock().lock();
            for (int i = 0; i < eventNode.size(); i++) {
                JSONObject item = (JSONObject) eventNode.get(i);
                host = (String) item.get("host");
                port = (String) item.get("port");
                key = host + port;
                if (!nodeMap.containsKey(key)) {
                    singleNodeMap = new HashMap();
                    singleNodeMap.put("host", host);
                    singleNodeMap.put("port", port);
                    nodeMap.put(key, singleNodeMap);
                }
            }

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
            nodelock.writeLock().unlock();

            rwl.writeLock().lock();
            for (int i = 0; i < eventMap.size(); i++) {
                JSONObject item = (JSONObject) eventMap.get(i);
                long eventid = (Long) item.get("eventid");
                String eventname = (String) item.get("eventname");
                long userid = (Long) item.get("userid");
                long avail = (Long) item.get("avail");
                long purchased = (Long) item.get("purchased");
                createNewEvent(eventid, eventname, userid, avail, purchased);
            }
            rwl.writeLock().unlock();

            timestamplock.writeLock().lock();

            for (int i = 0; i < timestamps.size(); i++) {
                JSONObject item = (JSONObject) timestamps.get(i);
                String timestamp = (String) item.get("timestamp");
                timeStampsSet.add(timestamp);
            }
            timestamplock.writeLock().unlock();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getNodeList() {
        JSONObject obj = new JSONObject();

        nodelock.readLock().lock();
        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();
        for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
            String key = entry.getKey();
            item = getNodeInfo(nodeMap, key);
            jsonArray.add(item);
        }
        obj.put("eventnode", jsonArray);
        nodelock.readLock().unlock();

        nodelock.readLock().lock();
        jsonArray = new JSONArray();
        item = new JSONObject();
        for (Map.Entry<String, HashMap<String, String>> entry : frontEndMap.entrySet()) {
            String key = entry.getKey();
            item = getNodeInfo(frontEndMap, key);
            jsonArray.add(item);
        }
        obj.put("frontendnode", jsonArray);
        nodelock.readLock().unlock();

        timestamplock.readLock().lock();
        jsonArray = new JSONArray();
        item = new JSONObject();
        Iterator iterator = timeStampsSet.iterator();

        while (iterator.hasNext()) {
            item.put("timestamp", iterator.next());
            jsonArray.add(item);
        }
        obj.put("timestamps", jsonArray);
        timestamplock.readLock().unlock();

        item = toNodeJsonObject(PrimaryHost, PrimaryPort);
        obj.put("primary", item);

        return obj.toString();
    }

    public String getFollowerJsonString() {
        JSONObject obj = new JSONObject();
        JSONObject item = new JSONObject();
        item = toNodeJsonObject(FollowerHost, FollowerPort);
        obj.put("follower", item);
        return obj.toString();
    }

    public String getPrimaryJsonString() {
        JSONObject obj = new JSONObject();
        JSONObject item = new JSONObject();
        item = toNodeJsonObject(PrimaryHost, PrimaryPort);
        obj.put("primary", item);
        return obj.toString();
    }

    public JSONObject getNodeInfo(Map<String, HashMap<String, String>> nodeMap, String key) {
        JSONObject s = new JSONObject();
        nodelock.readLock().lock();
        if (nodeMap.containsKey(key)) {
            String host, port;
            singleNodeMap = nodeMap.get(key);
            host = singleNodeMap.get("host");
            port = singleNodeMap.get("port");

            s = toNodeJsonObject(host, port);
        }
        nodelock.readLock().unlock();
        return s;
    }


    public JSONObject toNodeJsonObject(String host, String port) {
        JSONObject json = new JSONObject();
        json.put("host", host);
        json.put("port", port);
        return json;
    }


    public void setFollowerHost(String followerHost) {
        FollowerHost = followerHost;
    }

    public void setFollowerPort(String followerPort) {
        FollowerPort = followerPort;
    }

    public void setPrimaryHost(String primaryHost) {
        PrimaryHost = primaryHost;
    }

    public void setPrimaryPort(String primaryPort) {
        PrimaryPort = primaryPort;
    }

    public String getFollowerPort() {
        return FollowerPort;
    }

    public String getFollowerHost() {
        return FollowerHost;
    }

    public String getPrimaryHost() {
        return PrimaryHost;
    }

    public String getPrimaryPort() {
        return PrimaryPort;
    }
}
