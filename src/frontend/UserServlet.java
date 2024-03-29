package frontend;

import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import static frontend.FrontEndServer.USER_HOST;
import static frontend.FrontEndServer.USER_PORT;

/**
 * Handle with Users
 */
public class UserServlet extends BaseServlet {
    /**
     * GET /users/{userid}
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ArrayList<String> parameterList = getUrlParameterList(request);
        String userid;

        PrintWriter out = response.getWriter();
        String url;
        try {
            if (!parameterList.isEmpty()) {
                userid = parameterList.get(0);
                url = "http://" + USER_HOST + ":" + USER_PORT + "/" + userid;
                String responseS;
                responseS = sendGet(response, url);
                out.println(responseS);
            } else {
                response.setStatus(400);
            }

        } catch (Exception e) {
            response.setStatus(400);
            System.out.println(e);
        }

    }

    /**
     * /users/{userid}/tickets/transfer
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String url;
        String body = extractPostRequestBody(request);

        try {
            ArrayList<String> parameterList = getUrlParameterList(request);
            String userid;

            if (!(parameterList.isEmpty())) {
                userid = parameterList.get(0);
                url = "http://" + USER_HOST + ":" + USER_PORT + "/" + userid + "/tickets/transfer";

                String responseS;
                responseS = sendPost(response, url, body);
                out.println(responseS);
            } else {
                response.setStatus(400);
            }
        } catch (Exception e) {
            response.setStatus(400);
            System.out.println(e);
        }
    }
}
