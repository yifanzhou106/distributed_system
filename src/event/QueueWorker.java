package event;

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class QueueWorker {

    private ReentrantReadWriteLock QueueLock;
    private EventDataMap edm;
    private TreeMap<Integer, QueueObject> queue;
    private QueueHandler qh;
    private ExecutorService threads;
    private CountDownLatch countdowntimer;


    /**
     * Create a thread keep the queue alive
     * @param edm
     */
    public QueueWorker(EventDataMap edm) {
        this.edm = edm;
        threads = Executors.newCachedThreadPool();
        QueueLock = new ReentrantReadWriteLock();
        queue = new TreeMap<>();
        qh = new QueueHandler();
        countdowntimer = new CountDownLatch(1);
        threads.submit(qh);

    }

    /**
     * Add a object into queue
     * Check if wake up
     * @param obj
     */
    public  void enqueue(QueueObject obj) {
//        System.out.println("before enqueue");
        QueueLock.writeLock().lock();
//        System.out.println("in enqueue");
        try {
            System.out.println("\nEnqueueing received vid is " + obj.getVersionID());
            System.out.println("Current local vid is " + edm.getVersionID());
            queue.put(obj.getVersionID(), obj);
            System.out.println(queue.size());
            if (obj.getVersionID() == edm.getVersionID() + 1) {
                System.out.println("Wake up " + obj.getVersionID());
//                qh.wakeup();
                countdowntimer.countDown();
//                System.out.println("Now Wake up thread");
            }
        }finally {
            QueueLock.writeLock().unlock();
//            System.out.println("after enqueue");
        }
    }

    /**
     * Remove a object from queue
     */
    public  void dequeue() {
        QueueLock.writeLock().lock();
        try {
            System.out.println(queue.size());
            queue.remove(queue.firstEntry().getKey());
            System.out.println(queue.size());
        }finally {
            QueueLock.writeLock().unlock();
        }
    }

    /**
     * Ckeck if there is a object in the queue meet condition
     * @return
     */
    public  Boolean ifSleep() {
//        System.out.println("begin ifsleep");
        QueueLock.readLock().lock();
//        System.out.println("in ifsleep");

        try {
            Boolean flag = false;
            if (queue.isEmpty()) {
                flag = true;
            } else {
                System.out.println("\nifSleep queue obj vid: " + queue.get(queue.firstKey()).getVersionID());
                System.out.println("ifSleep Local vid: " + (edm.getVersionID()) + "+1");
                if (queue.get(queue.firstKey()).getVersionID() != (edm.getVersionID() + 1))
                    flag = true;
            }
            return flag;
        }finally {
            QueueLock.readLock().unlock();
//            System.out.println("after ifsleep");
        }
    }

    public class QueueHandler implements Runnable {
        @Override
        public synchronized void run() {
            while (true) {
                worker();
            }
        }
        public synchronized void wakeup() {
            System.out.println(" notifyall");
            this.notifyAll();
        }

        public void worker() {
            long eventid;
            long userid;
            String eventname;
            long numtickets;
            long tickets;
            try {
                if (ifSleep()) {
//                    this.wait();
                    countdowntimer.await();
                }
                System.out.println("\nWake up, begin read queue");
                QueueLock.readLock().lock();
//                System.out.println("in queue read lock");
                QueueObject obj = queue.firstEntry().getValue();
                QueueLock.readLock().unlock();
//                System.out.println("out queue read lock");

                System.out.println("Writing obj #" + obj.getVersionID());
                System.out.println(obj.getBody().toString());

                eventid = (Long) obj.getBody().get("eventid");
                if (obj.getPath().equals("create")) {
                    userid = (Long) obj.getBody().get("userid");
                    eventname = (String) obj.getBody().get("eventname");
                    numtickets = (Long) obj.getBody().get("numtickets");
                    edm.createNewEvent(eventid, eventname, userid, numtickets, 0);
                    System.out.println("Create successfully");


                } else if (obj.getPath().equals("purchase")) {
                    tickets = (Long) obj.getBody().get("tickets");
                    edm.purchaseTicket(eventid, tickets);
                    System.out.println("Purchase successfully");

                }
                obj.setFinishFlag(true);
                dequeue();
                edm.getVersionIDIncreased();
                countdowntimer = new CountDownLatch(1);
                System.out.println("Writing finished");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
