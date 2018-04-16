package frontend;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;

import java.util.*;

/**
 * Provides base functionality to all servlets.
 */
public class BaseServlet extends HttpServlet {

    protected void printRequest(HttpServletRequest httpRequest) {
        System.out.println(" \n\n Headers");

        Enumeration headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            System.out.println(headerName + " = " + httpRequest.getHeader(headerName));
        }

        System.out.println("\n\nParameters");

        Enumeration params = httpRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = (String) params.nextElement();
            System.out.println(paramName + " = " + httpRequest.getParameter(paramName));
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
            if (i == 4 || i == 6)
                parameterList.add(urlTokenList[i]);
        }
        return parameterList;
    }

    // HTTP GET request
    protected String sendGet(HttpServletResponse response, String url) throws Exception {


        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "HTTP/1.1");
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer responseStr = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseStr.append(inputLine);
            }

            in.close();

            return responseStr.toString();
        } else
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        return "";
    }

    // HTTP POST request
    protected String sendPost(HttpServletResponse response, String url, String urlParameters) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "HTTP/1.1");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

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
            StringBuffer responseStr = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseStr.append(inputLine);
            }
            in.close();

            return responseStr.toString();
        } else
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        return "";

    }

    protected String getTimeStamp(String Host, int Port) {
        String timestamp;
        UUID idOne = UUID.randomUUID();
        timestamp = Host + Port + idOne;
        return timestamp;
    }

}