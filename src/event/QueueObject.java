package event;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class QueueObject {

    public Boolean finishFlag = false;
    private String method;
    private String path;
    private String body;
    private int VersionID;


    public QueueObject(String path, String method, String body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    public JSONObject getBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(body);
            jsonObject = (JSONObject) jsonObj;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public int getVersionID() {
        VersionID = Integer.parseInt((String) getBody().get("vid"));
        return VersionID;
    }

    public Boolean getFinishFlag() {
        return finishFlag;
    }

    public void setFinishFlag(Boolean finishFlag) {
        this.finishFlag = finishFlag;
    }
}
