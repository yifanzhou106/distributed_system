package frontend.replication;


import frontend.BaseServlet;
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
public class UpdatePrimaryNodeServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String body = extractPostRequestBody(request);
        System.out.println(body);
        try {
            JSONObject obj = readJsonObj(body);
            JSONObject item = (JSONObject) obj.get("primary");
            EVENT_HOST = (String) item.get("host");
            EVENT_PORT = (String) item.get("port");
            System.out.println("Now new primary host is " + EVENT_HOST + EVENT_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
