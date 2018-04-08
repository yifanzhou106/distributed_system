package event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
    private static int PORT = 5650;

    static int USER_PORT = 2000;
    static String USER_HOST = "mc01";

    public static void main(String[] args) {
        EventServer es = new EventServer();
        Server server = new Server(PORT);
        EventDataMap edm = new EventDataMap();
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(new ServletHolder(new EventCreaterServlet(edm)), "/create");
        handler.addServletWithMapping(new ServletHolder(new EventPurchaseServlet(edm)), "/purchase/*");
        handler.addServletWithMapping(new ServletHolder(new EventGetterServlet(edm)), "/*");

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
}