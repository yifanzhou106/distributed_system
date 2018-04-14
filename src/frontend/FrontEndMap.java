package frontend;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FrontEndMap {

    private ReentrantReadWriteLock rwl;
    Timestamp time = new Timestamp(System.currentTimeMillis());

    private String PrimaryHost;
    private String PrimaryPort;
    private String Host;
    private String Port;

    private String timestamp;

    public FrontEndMap() {
        rwl = new ReentrantReadWriteLock();
    }

    public String getTimeStamp() {
      String s;
      s = Host+ Port + time.getTime();
        return timestamp;
    }

    public void setHost(String host) {
        Host = host;
    }

    public void setPort(String port) {
        Port = port;
    }

    public void setPrimaryPort(String primaryPort) {
        PrimaryPort = primaryPort;
    }

    public void setPrimaryHost(String primaryHost) {
        PrimaryHost = primaryHost;
    }

    public JSONObject toJsonObject(String host, String port) {
        JSONObject json = new JSONObject();
        json.put("host", host);
        json.put("port", port);
        return json;
    }
}
