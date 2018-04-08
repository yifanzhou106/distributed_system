package frontend;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static frontend.FrontEndServer.*;

/**
 * Create user, send info to users server
 */
public class CreateUserServlet extends BaseServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        response.setStatus(400);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        printRequest(request);
        PrintWriter out = response.getWriter();

        try {
            String s = extractPostRequestBody(request);
            String url = "http://" + USER_HOST + ":" + USER_PORT + "/create";
            String responseS;
            responseS = sendPost(response, url, s);
            out.println(responseS);
        } catch (Exception e) {
            response.setStatus(400);
            e.printStackTrace();
        }
    }


}
