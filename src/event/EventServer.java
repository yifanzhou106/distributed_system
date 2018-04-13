package event;

import event.replication.FindNodeServlet;
import event.replication.NodeElectionServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import event.EventBaseServlet;

/**
 * Demonstrates simple user registration, login, and session tracking. This
 * is a simplified example, and **NOT** secure.
 * This comprehensive example is provided by Prof. Engle.
 */
public class EventServer {
    protected static Logger log = LogManager.getLogger();
    public static String HOST = "localhost";
    public static int PORT = 5650;
    static int USER_PORT = 2000;
    static String USER_HOST = "mc01";
    private EventDataMap edm;
    public EventServer(){
        edm = new EventDataMap();
        Thread heartBeat = new Thread(new HeartBeatMessage(edm));
        heartBeat.start();
    }

    public static void main(String[] args) {

        // Needs host,port input
        EventServer es = new EventServer();
        Server server = new Server(PORT);
        es.edm.setFollowerHost(HOST);
        es.edm.setFollowerPort(String.valueOf(PORT));

        try {
            String responseS;
            String url = "http://localhost:7000/nodes";
            String s;
            s = es.edm.getFollowerJsonString();
            responseS = es.sendReplicationPost(url,s);
            es.edm.addNode(responseS);
            System.out.println(responseS);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(new ServletHolder(new EventCreaterServlet(es.edm)), "/create");
        handler.addServletWithMapping(new ServletHolder(new EventPurchaseServlet(es.edm)), "/purchase/*");
        handler.addServletWithMapping(new ServletHolder(new FindNodeServlet(es.edm)), "/nodes");
        handler.addServletWithMapping(new ServletHolder(new EventAddServlet(es.edm)), "/nodes/add");
        handler.addServletWithMapping(new ServletHolder(new NodeElectionServlet(es.edm)), "/nodes/election");


        handler.addServletWithMapping(new ServletHolder(new EventGetterServlet(es.edm)), "/*");

        server.setHandler(handler);

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

    // HTTP GET request
    public String sendGet( String url) throws Exception {

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
    public String sendReplicationPost( String url, String urlParameters) throws Exception {

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