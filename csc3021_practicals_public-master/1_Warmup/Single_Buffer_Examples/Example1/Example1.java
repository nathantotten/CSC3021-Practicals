/*===========================================================
  Naive approach to the implementation of a single-number
  buffer accessed by a producer thread and a consumer thread.


  The buffer is represented by a single integer variable.
  The buffer is declared as 'static' and so is global to both
  producer and consumer. That is, only one instance of it exists throughout
  execution of the program.


  Experiment with
  (i) Running several times and observing the last two numbers
  obtained by the consumer.
  (ii) Different values of MAX_NUMBER
  (iii) Starting the consumer before the producer.

  ===========================================================*/
class Example1 {
  static int buffer;
  static final int MAX_NUMBER = 10;
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
      number++;
      Example1.buffer = number;
      System.out.println("Producer puts " + number + " in buffer.");
    } while (number  != Example1.MAX_NUMBER);
  }
}

/*===========================================================
  Consumer process, reads numbers in the buffer
  ===========================================================*/
class Consumer extends Thread {

  private int number;

  public void run() {
    do {
      number = Example1.buffer;
      System.out.println("Consumer gets " + number + " from buffer.");
    } while (number != Example1.MAX_NUMBER);
  }
}
