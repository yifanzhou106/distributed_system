package event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provides base functionality to all servlets in this example.
 * Example of Prof. Engle
 */
public class EventBaseServlet extends HttpServlet {

    protected void printRequest(HttpServletRequest httpRequest) {
        System.out.println(" \n\n Headers");

        Enumeration headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            System.out.println(headerName + " = " + httpRequest.getHeader(headerName));
        }
    }


    protected String extractPostRequestBody(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = null;
            try {
                s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }

    protected Object readJsonObj(String objString, String key) throws Exception {
        JSONParser parser = new JSONParser();
        Object jsonObj = parser.parse(objString);
        JSONObject jsonObject = (JSONObject) jsonObj;
        return jsonObject.get(key);
    }

    protected ArrayList<String> getUrlParameterList(HttpServletRequest request) {
        ArrayList<String> parameterList = new ArrayList<>();
        String url = request.getRequestURL().toString();
        System.out.println(url);
        String[] urlTokenList = url.split("/", 0);
        for (int i = 0; i < urlTokenList.length; i++) {
            System.out.println(urlTokenList[i]);
            parameterList.add(urlTokenList[i]);
        }
        return parameterList;
    }


    // HTTP POST request
    protected String sendPost(String url, String urlParameters) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "HTTP/1.1");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();
        }
        return "";

    }
}