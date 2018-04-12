package event;

import event.replication.FindNodeServlet;
import event.replication.NodeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Demonstrates simple user registration, login, and session tracking. This
 * is a simplified example, and **NOT** secure.
 * This comprehensive example is provided by Prof. Engle.
 */
public class EventServer {
    protected static Logger log = LogManager.getLogger();
    public static int PORT = 5650;
    static int USER_PORT = 2000;
    static String USER_HOST = "mc01";
    private EventDataMap edm;
    public EventServer(){
        String responseS;
        edm = new EventDataMap();
        try {
            String url = "http://localhost:6000/nodes";
            responseS = sendGet(url);
            edm.addNode(responseS);
            System.out.println(responseS);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventServer es = new EventServer();
        Server server = new Server(PORT);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(new ServletHolder(new EventCreaterServlet(es.edm)), "/create");
        handler.addServletWithMapping(new ServletHolder(new EventPurchaseServlet(es.edm)), "/purchase/*");
        handler.addServletWithMapping(new ServletHolder(new FindNodeServlet(es.nm)), "/nodes");
        handler.addServletWithMapping(new ServletHolder(new EventAddServlet(es.edm)), "/nodes/add");

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
}