/*===========================================================

===========================================================*/
import java.util.concurrent.Semaphore;
import java.util.Random;

class Time {
    public static void delay( int msec ) {
	// Pause thread for specified number of milliseconds
	try {
	    Thread.sleep( msec );
	} catch( InterruptedException e ) {
	    Thread.currentThread().interrupt();
	}
    }
}

class Example2 {
    static final int MAX_NUMBER = 5;
    static final int BUFFER_SIZE = 4;

    static int [] buffer = new int [BUFFER_SIZE];
    static int head =0, tail = 0;
    static Semaphore elements = new Semaphore(0);
    static Semaphore spaces = new Semaphore(BUFFER_SIZE);
    static Semaphore mutex = new Semaphore(1);


    public static void main (String[] args) {

        System.out.println("Start of program.");
        Producer producer  = new Producer(1);
        Producer producer2 = new Producer(2);
        Consumer consumer  = new Consumer();

        producer.start();
        producer2.start();
        consumer.start();
    }
}
/*===========================================================
    Producer process, puts numbers in the buffer
===========================================================*/
class Producer extends Thread {
    private int number = 0;
    private int id;

    public Producer(int id) {
        this.id=id;
    }

    public void run() {
	Random rnd = new Random();
        do {
            Example2.spaces.acquireUninterruptibly();
              Time.delay(rnd.nextInt(20));
            int i=produce();
              Time.delay(rnd.nextInt(20));
//            Example2.mutex.acquireUninterruptibly();
            Example2.buffer[Example2.tail]=i;
              Time.delay(rnd.nextInt(20));
            Example2.tail=(Example2.tail+1)% Example2.BUFFER_SIZE;
//            Example2.mutex.release();
              Time.delay(rnd.nextInt(20));
            Example2.elements.release();
              Time.delay(rnd.nextInt(20));
            System.out.println("P"+id+": "+i+" added to buffer");
        } while (number  != Example2.MAX_NUMBER);
        System.out.println("P"+id+": finished");
    }

    public int produce() {
        number++;
        return id*number;
    }

}


/*===========================================================
    Consumer process, reads numbers in the buffer
===========================================================*/
class Consumer extends Thread {

    private int number=0;

    public void run() {
	Random rnd = new Random();
        do {
            Example2.elements.acquireUninterruptibly();
	    Time.delay(rnd.nextInt(20));
            int i=Example2.buffer[Example2.head];
              Time.delay(rnd.nextInt(20));
            Example2.head=(Example2.head+1)% Example2.BUFFER_SIZE;
              Time.delay(rnd.nextInt(20));
            consume(i);
              Time.delay(rnd.nextInt(20));
            Example2.spaces.release();
        } while (number != 2*Example2.MAX_NUMBER);
         System.out.print("C : finished");
    }

    public void consume(int i) {
        System.out.println("C : "+i+" read from buffer");
        number++;
    }
}
