package frontend.replication;


import frontend.BaseServlet;
import frontend.FrontEndMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import static frontend.FrontEndServer.EVENT_HOST;
import static frontend.FrontEndServer.EVENT_PORT;

/**
 * Create events
 */
public class FindNodeServlet extends BaseServlet {
    private FrontEndMap fem;

    public FindNodeServlet(FrontEndMap fem) {
        this.fem = fem;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        PrintWriter out = response.getWriter();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        PrintWriter out = response.getWriter();
        String s = extractPostRequestBody(request);
        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(s);
            JSONObject obj = (JSONObject) jsonObj;
            JSONObject item = (JSONObject) obj.get("follower");
            EVENT_HOST = (String) item.get("host");
            EVENT_PORT = Integer.parseInt((String)item.get("port"));

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
