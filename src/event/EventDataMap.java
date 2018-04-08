package event;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handle with all Event info in a map
 */
public class EventDataMap {
    private Map<Long, HashMap<String, Object>> eventMap;
    private HashMap<String, Object> singleEventMap;
    private ReentrantReadWriteLock rwl;


    public EventDataMap() {
        eventMap = new TreeMap<>();
        rwl = new ReentrantReadWriteLock();
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
    public Boolean purchaseTacket(long eventid, long tickets) {
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
}
