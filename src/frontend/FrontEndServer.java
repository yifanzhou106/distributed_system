package frontend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Project 3 - Service-Oriented Ticket Purchase Application
 *
 * @Author Yifan Zhou
 */
public class FrontEndServer {
    protected static Logger log = LogManager.getLogger();
    private static int PORT = 5600;
    static int EVENT_PORT = 5650;
    static String EVENT_HOST = "mc12";
    static int USER_PORT = 2000;
    static String USER_HOST = "mc01";

    public static void main(String[] args) {
        Server server = new Server(PORT);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ServletContextHandler context = new ServletContextHandler();

        context.addServlet(CreateUserServlet.class, "/users/create");
        context.addServlet(UserServlet.class, "/users/*");
        context.addServlet(EventServlet.class, "/events");
        context.addServlet(CreateEventServlet.class, "/events/create");
        context.addServlet(EventServlet.class, "/events/*");
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
}