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
    private ReentrantReadWriteLock eventnodelock;
    private ReentrantReadWriteLock frontendnodelock;
    private ReentrantReadWriteLock VersionIDLock;

    private ReentrantReadWriteLock timestamplock;


    private Map<String, HashMap<String, String>> nodeMap;
    private Map<String, HashMap<String, String>> frontEndMap;
    private HashMap<String, String> singleNodeMap;

    private HashSet<String> timeStampsSet;
    private String PrimaryHost;
    private String PrimaryPort;

    private String FollowerHost;
    private String FollowerPort;
    private int VersionID;


    public EventDataMap() {
        eventMap = new TreeMap<>();
        nodeMap = new TreeMap<>();
        frontEndMap = new TreeMap<>();
        rwl = new ReentrantReadWriteLock();
        eventnodelock = new ReentrantReadWriteLock();
        frontendnodelock = new ReentrantReadWriteLock();
        VersionIDLock = new ReentrantReadWriteLock();
        VersionID = 0;
        timestamplock = new ReentrantReadWriteLock();
        timeStampsSet = new HashSet<>();

        try {
//            PrimaryHost = InetAddress.getLocalHost().toString();
            PrimaryHost = "localhost";
            PrimaryPort = "7000";
//            FollowerHost = InetAddress.getLocalHost().toString();
            FollowerHost = "localhost";
            FollowerPort = "7050";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Create a random event id
     *
     * @return
     */
    public Long createRandomEventId() {
        rwl.readLock().lock();
        try {
            Random rand = new Random();
            long id;
            do {
                id = rand.nextInt(10000);
            } while (eventMap.containsKey(id));
            return id;
        } finally {
            rwl.readLock().unlock();
        }


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
        rwl.writeLock().lock();
        try {
            singleEventMap = new HashMap();
            singleEventMap.put("eventname", eventname);
            singleEventMap.put("userid", userid);
            singleEventMap.put("avail", avail);
            singleEventMap.put("purchased", purchased);
            eventMap.put(eventid, singleEventMap);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Get event list
     *
     * @return
     */
    public JSONArray getEventList() {
        rwl.readLock().lock();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject item = new JSONObject();
            for (Map.Entry<Long, HashMap<String, Object>> entry : eventMap.entrySet()) {
                long eventid = entry.getKey();
                item = getEventInfo(eventid);
                jsonArray.add(item);
            }

            return jsonArray;
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Get single event info
     *
     * @param eventid
     * @return
     */
    public JSONObject getEventInfo(Long eventid) {
        rwl.readLock().lock();
        try {
            JSONObject s = new JSONObject();
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
            return s;
        } finally {
            rwl.readLock().unlock();

        }
    }

    /**
     * Purchase tickets and do post to user
     *
     * @param eventid
     * @param tickets
     * @return
     */
    public Boolean purchaseTicket(long eventid, long tickets) {
        rwl.writeLock().lock();
        try {
            Boolean isSuccess = false;

            if (eventMap.containsKey(eventid)) {
                String eventname;
                long userid, avail, purchased;
                singleEventMap = eventMap.get(eventid);
                eventname = (String) singleEventMap.get("eventname");
                userid = (Long) singleEventMap.get("userid");
                avail = (Long) (singleEventMap.get("avail"));
                purchased = (Long) (singleEventMap.get("purchased"));
                if (avail - tickets >= 0) {
                    avail = avail - tickets;
                    purchased = purchased + tickets;
                    isSuccess = true;
                    eventMap.remove(eventid);
                    createNewEvent(eventid, eventname, userid, avail, purchased);
                } else {
                    System.out.println("Neg cases: " + ((Long) (singleEventMap.get("avail")) - tickets));
                }
            }
            return isSuccess;
        } finally {
            rwl.writeLock().unlock();
        }
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

    /**
     * getNodeMap
     *
     * @return
     */
    public Map<String, HashMap<String, String>> getNodeMap() {
        eventnodelock.readLock().lock();
        try {
            Map<String, HashMap<String, String>> nodemap = nodeMap;
            return nodemap;

        } finally {
            eventnodelock.readLock().unlock();
        }

    }

    /**
     * getFrontEndMap
     *
     * @return
     */
    public Map<String, HashMap<String, String>> getFrontEndMap() {
        frontendnodelock.readLock().lock();
        try {
            Map<String, HashMap<String, String>> nodemap = frontEndMap;
            return nodemap;
        }finally {
            frontendnodelock.readLock().unlock();

        }
    }

    /**
     * Check Primary
     *
     * @return
     */
    public Boolean isPrimary() {
//        System.out.println("Primary: " + PrimaryHost + PrimaryPort);
//        System.out.println("I am: " + FollowerHost + FollowerPort);
        return PrimaryHost.equals(FollowerHost) && PrimaryPort.equals(FollowerPort);
    }

    /**
     * Check if timestamp existed
     *
     * @param timestamp
     * @return
     */
    public Boolean isTimeStampExist(String timestamp) {
        Boolean is_exist = false;
        timestamplock.readLock().lock();
        if (timeStampsSet.contains(timestamp))
            is_exist = true;
        timestamplock.readLock().unlock();

        return is_exist;
    }

    /**
     * Add timestamp into map
     *
     * @param timestamp
     */
    public void addTimeStamp(String timestamp) {
        timestamplock.writeLock().lock();
        timeStampsSet.add(timestamp);
        timestamplock.writeLock().unlock();

    }

    /**
     * Add single node into nodemap
     *
     * @param host
     * @param port
     */
    public void addSingleNode(String host, String port) {
        eventnodelock.writeLock().lock();
        try {
            singleNodeMap = new HashMap();
            String key = host + port;
            singleNodeMap.put("host", host);
            singleNodeMap.put("port", port);
            nodeMap.put(key, singleNodeMap);
        }finally {
            eventnodelock.writeLock().unlock();
        }

    }

    /**
     * Add single node into frontend map
     *
     * @param host
     * @param port
     */
    public void addSingleFrontendNode(String host, String port) {
        frontendnodelock.writeLock().lock();
        try {
            singleNodeMap = new HashMap();
            String key = host + port;
            singleNodeMap.put("host", host);
            singleNodeMap.put("port", port);
            frontEndMap.put(key, singleNodeMap);
        }finally {
            frontendnodelock.writeLock().unlock();
        }

    }

    /**
     * Remove a single node from nodemap
     *
     * @param host
     * @param port
     */
    public void removeSingleNode(String host, String port, Map<String, HashMap<String, String>> nodeMap) {
        eventnodelock.writeLock().lock();
        try {
            String key = host + port;
            nodeMap.remove(key);
        }finally {
            eventnodelock.writeLock().unlock();
        }

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

            eventnodelock.writeLock().lock();
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
            eventnodelock.writeLock().unlock();

            frontendnodelock.writeLock().lock();
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
            frontendnodelock.writeLock().unlock();

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

            VersionIDLock.writeLock().lock();
            VersionID = Integer.parseInt((String) JsonObj.get("vid"));
            VersionIDLock.writeLock().unlock();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray getEventNodeList() {
        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();
        for (Map.Entry<String, HashMap<String, String>> entry : nodeMap.entrySet()) {
            String key = entry.getKey();
            item = getNodeInfo(nodeMap, key);
            jsonArray.add(item);
        }
        return jsonArray;
    }

    public JSONArray getFrontendNodeList() {
        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();
        for (Map.Entry<String, HashMap<String, String>> entry : frontEndMap.entrySet()) {
            String key = entry.getKey();
            item = getNodeInfo(frontEndMap, key);
            jsonArray.add(item);
        }
        return jsonArray;
    }

    public String getNodeList() {
        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();

        rwl.readLock().lock();
        jsonArray = getEventList();
        obj.put("eventmap", jsonArray);
        rwl.readLock().unlock();

        eventnodelock.readLock().lock();
        jsonArray = getEventNodeList();
        obj.put("eventnode", jsonArray);
        eventnodelock.readLock().unlock();

        frontendnodelock.readLock().lock();
        jsonArray = getFrontendNodeList();
        obj.put("frontendnode", jsonArray);
        frontendnodelock.readLock().unlock();

        timestamplock.readLock().lock();
        jsonArray = new JSONArray();
        item = new JSONObject();
        Iterator iterator = timeStampsSet.iterator();

        VersionIDLock.readLock().lock();
        obj.put("vid", String.valueOf(VersionID));
        VersionIDLock.readLock().unlock();

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
        eventnodelock.readLock().lock();
        try {
            JSONObject s = new JSONObject();
            if (nodeMap.containsKey(key)) {
                String host, port;
                singleNodeMap = nodeMap.get(key);
                host = singleNodeMap.get("host");
                port = singleNodeMap.get("port");
                s = toNodeJsonObject(host, port);
            }
            return s;
        }finally {
            eventnodelock.readLock().unlock();
        }
    }


    public JSONObject toNodeJsonObject(String host, String port) {
        JSONObject json = new JSONObject();
        json.put("host", host);
        json.put("port", port);
        return json;
    }


    public int getVersionID() {
        VersionIDLock.readLock().lock();
        try {
            return VersionID;
        } finally {
            VersionIDLock.readLock().unlock();
        }
    }

    public int getVersionIDIncreased() {
        VersionIDLock.writeLock().lock();
        try {
            VersionID++;
            return VersionID;
        } finally {
            VersionIDLock.writeLock().unlock();
        }
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
