package event;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class QueueWorker {

    private ReentrantReadWriteLock QueueLock;
    private EventDataMap edm;

    private TreeMap<Integer, QueueObject> queue;
    private QueueObject obj;
    private QueueHandler qh;
    private Thread qhT;

    public QueueWorker(EventDataMap edm) {
        this.edm = edm;

        QueueLock = new ReentrantReadWriteLock();
        queue = new TreeMap<>();
        qh = new QueueHandler();
        qhT = new Thread(qh);
        qhT.start();


    }

    public void enqueue(QueueObject obj) {
        QueueLock.writeLock().lock();
        System.out.println("\nEnqueueing received vid is " + obj.getVersionID() + "Current local vid is " + edm.getVersionID());
        queue.put(obj.getVersionID(), obj);
        if (obj.getVersionID() == edm.getVersionID() + 1) {
            System.out.println("Wake up " + obj.getVersionID());
            qh.wakeup();
        }
        QueueLock.writeLock().unlock();

    }

    public void dequeue() {

        QueueLock.writeLock().lock();
        queue.remove(queue.firstEntry().getKey());
        QueueLock.writeLock().unlock();
    }

    public Boolean ifSleep() {
        Boolean flag = false;
        QueueLock.readLock().lock();

        if (queue.isEmpty()) {
            flag = true;
        } else {
            System.out.println("\nifSleep queue obj vid: " + queue.get(queue.firstKey()).getVersionID());
            System.out.println("ifSleep Local vid: " + (edm.getVersionID()) + "+1");
            if (queue.get(queue.firstKey()).getVersionID() != (edm.getVersionID() + 1))
                flag = true;
        }
        QueueLock.readLock().unlock();
        return flag;
    }

    public class QueueHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                worker();
            }
        }

        public synchronized void wakeup() {
            notifyAll();
        }

        public synchronized void worker() {
            long eventid;
            long userid;
            String eventname;
            long numtickets;
            long tickets;
            try {
                if (ifSleep()) {
                    wait();
                }
                obj = queue.firstEntry().getValue();
                System.out.println("Writing obj #" + obj.getVersionID());

                eventid = (Long) obj.getBody().get("eventid");
                if (obj.getPath().equals("create")) {
                    userid = (Long) obj.getBody().get("userid");
                    eventname = (String) obj.getBody().get("eventname");
                    numtickets = (Long) obj.getBody().get("numtickets");
                    edm.createNewEvent(eventid, eventname, userid, numtickets, 0);

                } else if (obj.getPath().equals("purchase")) {
                    tickets = (Long) obj.getBody().get("tickets");
                    edm.purchaseTicket(eventid, tickets);
                }
                obj.setFinishFlag(true);
                dequeue();
                edm.increaseVid();
                System.out.println("After Increasing " + edm.getVersionID());
            } catch (Exception e) {
                System.out.println(444);
                e.printStackTrace();
            }

        }
    }


}
