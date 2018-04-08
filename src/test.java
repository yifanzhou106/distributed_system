
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A chat client that uses raw sockets to communicate with clients
 * Project 02
 * Last Version
 *
 * @Author Yifan Zhou
 */
public class test {
    public static String PORT = "5600";
     public static String HOST = "localhost";
    public final ExecutorService threads = Executors.newFixedThreadPool(10);

    public static volatile boolean isShutdown = false;

    /**
     * Main function load hotelData and reviews, Then call startServer.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        test client = new test();

        client.beginChat();
    }

    /**
     * create threads
     */
    public void beginChat() {
            for (int i = 0; i <1000; i++)
                threads.submit(new sendGetMessage());
        list();

    }

    void list (){
        try {
            String url = "http://" + HOST + ":" + PORT + "/events";
            System.out.println(sendGet(url));
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
    public class sendGetMessage implements Runnable {
        @Override
        public void run () {
//            create();
            purchase();
            list();

//            createEvent();
//            purchaseEvent();
    }

    void create (){
        try {
            String url = "http://" + HOST + ":" + PORT + "/events/create";
            String s = "{\"userid\":1001, \"eventname\":\"Dinner\", \"numtickets\": 20}";
            System.out.println(sendPost(url, s));
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
        void createEvent (){
            try {
                String url = "http://" + HOST + ":" + PORT + "/create";
                String s = "{\"userid\":1001, \"eventname\":\"Dinner\", \"numtickets\": 20}";
                System.out.println(sendPost(url, s));
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }

        void purchase (){
            try {
                String url = "http://" + HOST + ":" + PORT + "/events/"+54 +"/purchase/34";
                String s = "{\"tickets\":1}";
                System.out.println(sendPost(url, s));
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        void purchaseEvent (){
            try {
                String url = "http://" + HOST + ":" + PORT + "/purchase/5247";
                String s = "{\"eventid\":5247,\"userid\":34,\"tickets\":1}";
                System.out.println(sendPost(url, s));
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
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
    public String sendPost( String url, String urlParameters) throws Exception {

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