/*===========================================================


===========================================================*/
import java.util.concurrent.Semaphore;

class Example1 {
    static final int MAX_NUMBER = 10;
    static final int BUFFER_SIZE = 4;

    static int [] buffer = new int [BUFFER_SIZE];
    static int head =0, tail = 0;
    static Semaphore elements = new Semaphore(0);
    static Semaphore spaces = new Semaphore(BUFFER_SIZE);

    public static void main (String[] args) {

        System.out.println("Start of program.");
        Producer producer = new Producer();
        Consumer consumer = new Consumer();

        producer.start();
        consumer.start();
    }
}
/*===========================================================
    Producer process, puts numbers in the buffer
===========================================================*/
class Producer extends Thread {
    private int number = 0;
    public void run() {
        do {
            Example1.spaces.acquireUninterruptibly();
            int i=produce();
            Example1.buffer[Example1.tail]=i;
            Example1.tail=(Example1.tail+1)% Example1.BUFFER_SIZE;
            Example1.elements.release();
            System.out.println("P: "+i+" added to buffer");
        } while (number  != Example1.MAX_NUMBER);
        System.out.println("P: finished");
    }

    public int produce() {
        number++;
        return number;
    }

}

/*===========================================================
    Consumer process, reads numbers in the buffer
===========================================================*/
class Consumer extends Thread {

    private int number=0;

    public void run() {
        do {
            Example1.elements.acquireUninterruptibly();
            int i=Example1.buffer[Example1.head];
            Example1.head=(Example1.head+1)% Example1.BUFFER_SIZE;
            consume(i);
            Example1.spaces.release();
        } while (number != Example1.MAX_NUMBER);
         System.out.print("C: finished");
    }

    public void consume(int i) {
        System.out.println("C: "+i+" read from buffer");
        number++;
    }
}
