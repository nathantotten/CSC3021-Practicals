/*----------------------------------------------------
  This program illustrates a simple example of a race
  condition.

  The variable amount is shared by both processes.
  Amount is initialised to 100. One process performs
  amount=amount+100, while the other performs
  amount=amount+200.

  What are the three possible final values of amount ?
  Run it several times and see if you can produce all
  three outputs.
  -------------------------------------------------------*/
import java.util.Random;

class race {
  public static int amount = 100 ;
  public static void main (String[] args) {

    Thread p1 = new Thread(new P1 ()) ;
    Thread p2 = new Thread(new P2 ()) ;
    Thread display = new Thread (new Display());

    p1.start () ;
    p2.start () ;
    display.start();
  }

}

class P1 implements Runnable {
  Random rnd = new Random();

  public void run ()  {
    Time.delay(rnd.nextInt(20));
    int temp = race.amount;
    System.out.println ("P1: Load");
    Time.delay(rnd.nextInt(10));
    temp = temp + 100;
    System.out.println ("P1: Increment");
    Time.delay(rnd.nextInt(10));
    race.amount = temp;
    System.out.println ("P1: Store");
  }
}

class P2 implements Runnable {
  Random rnd = new Random();

  public void run ()  {
    Time.delay(rnd.nextInt(10));
    int temp = race.amount;
    System.out.println ("P2: Load");
    Time.delay(rnd.nextInt(10));
    temp = temp + 200;
    System.out.println ("P2: Increment");
    Time.delay(rnd.nextInt(10));
    race.amount = temp;
    System.out.println ("P2: Store");
  }
}


class Display implements Runnable {
  public void run () {
    Time.delay (150);
    System.out.println ("amount = " + race.amount);
  }
}
