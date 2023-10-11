/*===========================================================
  In this example an attempt is made to coordinate the activities
  of the producer and consumer processes via a global variable
  called 'turn'.

  What happens if:
  the Producer thread dies; or
  the priority of the Producer > the priority of the Consumer.

  ===========================================================*/
import java.util.Random;

class Example2 {
  static int buffer;

  static volatile int turn = 1;

  static final int MAX_NUMBER = 10;

  public static void main (String[] args) {

    System.out.println("Start of program.");
    Producer producer = new Producer();
    Consumer consumer = new Consumer();
    //        Set Producer priority > Comsumer prioroity
            producer.setPriority(consumer.getPriority()+1);
    producer.start();
    consumer.start();
  }
}

/*===========================================================
  Producer process, puts numbers in the buffer
  ===========================================================*/
class Producer extends Thread {
  private int number = 0;
  private Random rnd = new Random();

  public void run() {
    do {
      while (Example2.turn != 1) {  
        System.out.println("P waiting");
        //                Thread.yield();
        Time.delay(rnd.nextInt(120));
      }
      number++;
      Example2.buffer = number;
      System.out.println("Producer puts " + number + " in buffer.");
      Example2.turn = 2;
      //            Kill the Producer
      //            Thread.currentThread().stop();
    } while (number  != Example2.MAX_NUMBER);
  }
}

/*===========================================================
  Consumer process, reads numbers in the buffer
  ===========================================================*/
class Consumer extends Thread {
  private int number;
  private Random rnd = new Random();

  public void run() {
    do {
      while (Example2.turn != 2)  {
        System.out.println("C waiting");
        //               Thread.yield();
        Time.delay(rnd.nextInt(120));
      }
      number = Example2.buffer;
      System.out.println("Consumer gets " + number + " from buffer.");
      Example2.turn = 1;
    } while (number != Example2.MAX_NUMBER);
  }
}
