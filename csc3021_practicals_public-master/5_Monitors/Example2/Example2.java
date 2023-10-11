/*===========================================================
  Updated
  Hans Vandierendonck September 2014
===========================================================*/
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
    static final int MAX_NUMBER = 10;
    public static void main (String[] args) {

        System.out.println("Start of program.");
        PCM pcm = new PCM();
        Producer producer  = new Producer(pcm,0);
        Consumer consumer0  = new Consumer(pcm,0);
        Consumer consumer1  = new Consumer(pcm,1);

        producer.start();
        consumer0.start();
        consumer1.start();
    }
}

class Producer extends Thread {
    private Random rnd = new Random();
    private int number = 0;
    private PCM pcm;
    private int id;

    public Producer(PCM pcm, int id) {
        this.pcm=pcm;
        this.id = id;
    }

    public void run() {
        do {
            int i=produce();
            pcm.put(i);
            Time.delay(rnd.nextInt(120));
        } while (number  != Example2.MAX_NUMBER);
        System.out.println("P: finished");
    }

    public int produce() {
        number++;
        return number;
    }
}

class Consumer extends Thread {
    private Random rnd = new Random();
    private int number = 0;
    private PCM pcm;
    private int id;

    public Consumer(PCM pcm, int id) {
        this.pcm=pcm;
        this.id=id;
    }

    public void run() {
        do {
            int i= pcm.get(id);
            consume(i);
            Time.delay(rnd.nextInt(120));
        } while (number  != Example2.MAX_NUMBER);
        System.out.println("C: finished");
    }

    public void consume(int i) {
        number++;
    }
}

class PCM {
  private int N = 4;
  private int[] buffer = new int [N];
  private int tail = 0, head = 0;
  private int count = 0;

  public synchronized void put (int i)
  {
//    while (count == N) try {wait();} catch (InterruptedException e){}
    if (count == N) try {wait();} catch (InterruptedException e){}
    buffer[tail] = i;
    tail = (tail + 1) % N;
    count++;
    System.out.println("P : "+i+" put in buffer");
    notifyAll();
  }
  public synchronized int get(int id)
  { int i;
//    while (count == 0) try {wait();} catch (InterruptedException e){}
    if (count == 0) try {wait();} catch (InterruptedException e){}
    i = buffer[head];
    head = (head + 1) % N;
    count--;
    System.out.println("C"+id+": "+i+" read from buffer");
    notifyAll();
    return i;
  }
}
