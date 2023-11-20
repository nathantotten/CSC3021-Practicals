/*----------------------------------------------------
  This program illustrates the idea of a concurrent
  program as an interleaving of statements.

  Run it several times and observe the output.
  Repeat with the delay statements commented out.
  -------------------------------------------------------*/

import java.util.Random;

class Time2 {
  public static void delay( int msec ) {
    // Pause thread for specified number of milliseconds
    try {
      Thread.sleep( msec );
    } catch( InterruptedException e ) {
      Thread.currentThread().interrupt();
    }
  }
}

class Interleave {

  public static int c1 = 2 ;
  public static int c2 = 3 ;

  public static void main (String[] args) {

    Thread p1 = new P1_interleave() ;
    Thread p2 = new P2_interleave() ;
    Thread display = new Display_interleave();

    p2.start () ;
    p1.start () ;
    display.start();
  }
}

class P1_interleave extends Thread {
  public void run ()  {
    Random rnd = new Random();
    //Time.delay(rnd.nextInt(20));
    Interleave.c1 = Interleave.c1 * Interleave.c2;
    System.out.println ("P1 finished");
  }
}

class P2_interleave extends Thread {
  public void run () {
    Random rnd = new Random();
    //Time.delay(rnd.nextInt(20));
    Interleave.c1 = Interleave.c1 + Interleave.c2;
    System.out.println ("P2 finished");
  }
}


class Display_interleave extends Thread {
  public void run () {
    //Time.delay(100);
    System.out.println ("c1 = " + Interleave.c1 + "    c2 = " + Interleave.c2);
  }
}
