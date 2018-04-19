package frontend;

import frontend.replication.UpdatePrimaryNodeServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 * Project 4 - Replication
 *
 * @Author Yifan Zhou
 */
public class FrontEndServer {
    protected static Logger log = LogManager.getLogger();
    public static String HOST = "localhost";
    public static int PORT = 5700;
    public static volatile int EVENT_PORT = 7000;
    public static volatile String EVENT_HOST = "localhost";
    static int USER_PORT = 2000;
    static String USER_HOST = "mc01";


    public static void main(String[] args) {
        FrontEndServer fes = new FrontEndServer();
        if (args.length > 0) {
            if (args[0].equals("-localhost")) {
                HOST = args[1];
            }
            if (args[2].equals("-localport")) {
                PORT = Integer.parseInt(args[3]);
            }
            if (args[4].equals("-primaryhost")) {
                EVENT_HOST = args[5];
            }
            if (args[6].equals("-primarport")) {
                EVENT_PORT = Integer.parseInt(args[7]);
            }
        }
        Server server = new Server(PORT);

        fes.tellThemIamOn();

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        ServletContextHandler context = new ServletContextHandler();

        context.addServlet(CreateUserServlet.class, "/users/create");
        context.addServlet(UserServlet.class, "/users/*");
        context.addServlet(EventServlet.class, "/events");
        context.addServlet(CreateEventServlet.class, "/events/create");
        context.addServlet(EventServlet.class, "/events/*");
        context.addServlet(UpdatePrimaryNodeServlet.class, "/nodes");

        context.addServlet(OtherServlet.class, "/*");


        server.setHandler(context);

        log.info("Starting server on port " + PORT + "...");

        try {
            server.start();
            server.join();

            log.info("Exiting...");
        } catch (Exception ex) {
            log.fatal("Interrupted while running server.", ex);
            System.exit(-1);
        }
    }

    public void tellThemIamOn() {
        try {
//            String url = "http://localhost:5600/nodes";
//            String responseS;
//            responseS = sendGet(url);
//            String primeHost, primePort;
//            JSONParser parser = new JSONParser();
//            Object jsonObj = parser.parse(responseS);
//            JSONObject jsonObject = (JSONObject) jsonObj;
//            JSONObject prime = (JSONObject) jsonObject.get("primary");
//            primeHost = (String) prime.get("host");
//            primePort = (String) prime.get("port");
//
//            EVENT_HOST = primeHost;
//            EVENT_PORT = Integer.parseInt(primePort);

//            HOST = InetAddress.getLocalHost().toString();
            String responseS;
            HOST = "localhost";
            String url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/nodes/add/frontend";

            String s;
            JSONObject obj = new JSONObject();
            JSONObject item = new JSONObject();
            item.put("host", HOST);
            item.put("port", String.valueOf(PORT));
            obj.put("frontend", item);
            s = obj.toString();
            responseS = sendPost(url, s);
            System.out.println(responseS);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // HTTP GET request
    protected String sendGet(String url) throws Exception {


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
        }

        return "";
    }

    // HTTP POST request
    protected String sendPost(String url, String urlParameters) throws Exception {

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
        }
        return "";

    }
}