import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Test {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ReadWriteLock RW = new ReadWriteLock();    

        executorService.execute(new Reader(RW, 1));
        executorService.execute(new Reader(RW, 2));
        executorService.execute(new Writer(RW, 1));
        executorService.execute(new Writer(RW, 2));
        executorService.execute(new Reader(RW, 3));
        executorService.execute(new Reader(RW, 4));
        executorService.execute(new Writer(RW, 3));
        executorService.execute(new Writer(RW, 2));
    }
}



class ReadWriteLock {
    private Semaphore S = new Semaphore(1);
    private Semaphore readCountLock = new Semaphore(1);
    private Semaphore serviceQueue = new Semaphore(1); 
    private int readCount = 0;

    public void readLock() throws InterruptedException {
        serviceQueue.acquire();
        readCountLock.acquire();
        if (readCount == 0) {
            S.acquire();
        }
        readCount++;
        serviceQueue.release();
        readCountLock.release();
    }

    public void writeLock() throws InterruptedException {
        serviceQueue.acquire();
        S.acquire();
        serviceQueue.release();
    }

    public void readUnLock() throws InterruptedException {
        readCountLock.acquire();
        readCount--;
        if (readCount == 0) {
            S.release();
        }
        readCountLock.release();
    }

    public void writeUnLock() {
        S.release();
    }
}




class Writer implements Runnable {
    private ReadWriteLock RW_lock;
    private int id;

    public Writer(ReadWriteLock rw, int id) {
        RW_lock = rw;
        this.id = id;
    }

    public void run() {
        while (true) {
            try {
                RW_lock.writeLock();
                System.out.println("Writer " + id + " starts writing.");
                Thread.sleep(1000); 
                System.out.println("Writer " + id + " has finished writing.");
                RW_lock.writeUnLock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class Reader implements Runnable {
    private ReadWriteLock RW_lock;
    private int id;

    public Reader(ReadWriteLock rw, int id) {
        RW_lock = rw;
        this.id = id;
    }

    public void run() {
        while (true) {
            try {
                RW_lock.readLock();
                System.out.println("Reader " + id + " starts reading.");
                Thread.sleep(1000); 
                System.out.println("Reader " + id + " has finished reading.");
                RW_lock.readUnLock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
