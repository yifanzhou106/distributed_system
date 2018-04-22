package event;

import event.replication.AddFrontEndNodeServlet;
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
import java.net.InetAddress;
import java.net.URL;


/**
 * Demonstrates simple user registration, login, and session tracking. This
 * is a simplified example, and **NOT** secure.
 * This comprehensive example is provided by Prof. Engle.
 */
public class EventServer {
    protected static Logger log = LogManager.getLogger();
    public static String HOST = "localhost";
    public static int PORT = 7200;

    public static String EVENT_HOST = "localhost";
    public static String EVENT_PORT = "7200";

    static int USER_PORT = 2000;
    public static Boolean DEBUG = false;
    public static String DEBUG_NUM = "200";

    static String USER_HOST = "mc01";
    private EventDataMap edm;
    private QueueWorker qw;

    public EventServer() {
        edm = new EventDataMap();
        qw = new QueueWorker(edm);
    }

    public static void main(String[] args) {
        EventServer es = new EventServer();

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
            if (args[6].equals("-primaryport")) {
                EVENT_PORT = args[7];
            }
            if (args.length > 8)
                if (args[8].equals("-debug")) {
                    DEBUG = true;
                    DEBUG_NUM = args[9];

                }
        }
        // Needs host,port input
        Server server = new Server(PORT);
        es.tellThemIamOn();
        Thread heartBeat = new Thread(new HeartBeatMessage(es.edm));

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(new ServletHolder(new EventCreaterServlet(es.edm, es.qw)), "/create");
        handler.addServletWithMapping(new ServletHolder(new EventPurchaseServlet(es.edm, es.qw)), "/purchase/*");
        handler.addServletWithMapping(new ServletHolder(new FindNodeServlet(es.edm)), "/nodes");
        handler.addServletWithMapping(new ServletHolder(new AddFrontEndNodeServlet(es.edm)), "/nodes/add/frontend");
        handler.addServletWithMapping(new ServletHolder(new NodeElectionServlet(es.edm)), "/nodes/election");


        handler.addServletWithMapping(new ServletHolder(new EventGetterServlet(es.edm)), "/*");

        server.setHandler(handler);

        log.info("Starting server on port " + PORT + "...");

        try {
            server.start();
            heartBeat.start();
            server.join();

            log.info("Exiting...");
        } catch (Exception ex) {
            log.fatal("Interrupted while running server.", ex);
            System.exit(-1);
        }
    }

    public void tellThemIamOn() {
        try {
//            HOST = InetAddress.getLocalHost().toString();
//            HOST = "localhost";

//            edm.addSingleNode(HOST,String.valueOf(PORT));
            String responseS;
            String url = "http://" + EVENT_HOST + ":" + EVENT_PORT + "/nodes";
            System.out.println(url);
            String s;
            s = edm.getFollowerJsonString();
            responseS = sendReplicationPost(url, s);
            edm.addNode(responseS);
            System.out.println(responseS);

        } catch (Exception e) {
            System.out.println("Cannot connect to primary");
        }

    }

    // HTTP GET request
    public String sendGet(String url) throws Exception {

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
    public String sendReplicationPost(String url, String urlParameters) throws Exception {

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