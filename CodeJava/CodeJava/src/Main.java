import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class ResourcesExploiter {
    private int rsc;

    public void setRsc(int n) {
        rsc = n;
    }

    public int getRsc() {
        return rsc;
    }

    public ResourcesExploiter(int n) {
        rsc = n;
    }

    public void exploit() {
        setRsc(getRsc() + 1);
    }
}

class ThreadedWorkerWithoutSync extends Thread {
    private final ResourcesExploiter rExp;

    public ThreadedWorkerWithoutSync(ResourcesExploiter resource) {
        rExp = resource;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; ++i) {
            rExp.exploit();
        }
    }
}

class ThreadedWorkerWithSync extends Thread {
    private final ResourcesExploiter rExp;

    public ThreadedWorkerWithSync(ResourcesExploiter resource) {
        rExp = resource;
    }

    @Override
    public void run() {
        synchronized (rExp) {
            for (int i = 0; i < 1000; ++i) {
                rExp.exploit();
            }
        }
    }
}

class ResourcesExploiterWithLock extends ResourcesExploiter {
    private final ReentrantLock lock;

    public ResourcesExploiterWithLock(int n) {
        super(n);
        lock = new ReentrantLock();
    }

    @Override
    public void exploit() {
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                setRsc(getRsc() + 1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //release lock
            lock.unlock();
        }
    }
}

class ThreadedWorkerWithLock extends Thread {
    private final ResourcesExploiterWithLock rExp;

    public ThreadedWorkerWithLock(ResourcesExploiterWithLock resource) {
        rExp = resource;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; ++i) {
            rExp.exploit();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
//        ResourcesExploiter resource = new ResourcesExploiter(0);

//        ThreadedWorkerWithoutSync worker1 = new ThreadedWorkerWithoutSync(resource);
//        ThreadedWorkerWithoutSync worker2 = new ThreadedWorkerWithoutSync(resource);
//        ThreadedWorkerWithoutSync worker3 = new ThreadedWorkerWithoutSync(resource);

//        ThreadedWorkerWithSync worker1 = new ThreadedWorkerWithSync(resource);
//        ThreadedWorkerWithSync worker2 = new ThreadedWorkerWithSync(resource);
//        ThreadedWorkerWithSync worker3 = new ThreadedWorkerWithSync(resource);

        ResourcesExploiterWithLock resource = new ResourcesExploiterWithLock(0);
        ThreadedWorkerWithLock worker1 = new ThreadedWorkerWithLock(resource);
        ThreadedWorkerWithLock worker2 = new ThreadedWorkerWithLock(resource);
        ThreadedWorkerWithLock worker3 = new ThreadedWorkerWithLock(resource);

        worker1.start();
        worker2.start();
        worker3.start();

        worker1.join();
        worker2.join();
        worker3.join();

        System.out.println(resource.getRsc());
    }
}