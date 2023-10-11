/*===========================================================
    Java version of the Dekker's Algorithm

    This algorithm has all of the desired properties.


    G McClements 30/6/99; PLK February 2000; NSS July 2001
    HV September 2014

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

class Dekkers {
    public static volatile int c1 = 1 ;
    public static volatile int c2 = 1 ;
    public static volatile int turn = 1 ;

    public static void main (String[] args) {

        Thread p1 = new P1 () ;
        Thread p2 = new P2 () ;

        p1.start () ;
        p2.start () ;
    }
}

class P1 extends Thread {
    Random rnd = new Random();

    public void run ()  {
        while (true) {
            nonCriticalSection();
            preProtocol();
            criticalSection();
            postProtocol();
       }
    }

    public void nonCriticalSection() {
//       System.out.println ("1 ncc: Entering non-critical section" ) ;
      Time.delay(rnd.nextInt(120));
//       System.out.println ("1 ncc: Leaving non-critical section" ) ;
    }

    public void preProtocol() {
       Dekkers.c1 = 0 ;
       while (Dekkers.c2 != 1) 
          if (Dekkers.turn == 2) {
             Dekkers.c1 = 1 ;
             while (Dekkers.turn !=1) {
                System.out.println ("1 prep: Waiting for turn");
              }
              Dekkers.c1 = 0 ;
           }
       
    }

    public void criticalSection() {
       System.out.println("1 cs: Entering critical section ");
       Time.delay(rnd.nextInt(120));
       System.out.println("1 cs: Leaving critical section ");
    }

    public void postProtocol() {
       System.out.println("1 cs: Entering postProtocol section ");
       Dekkers.turn = 2;
       Time.delay(rnd.nextInt(120));
       Dekkers.c1 = 1 ;
       System.out.println("1 cs: Leaving postProtocol section ");
    }
}

class P2 extends Thread {
    Random rnd = new Random();

    public void run () {
        while (true) {
            nonCriticalSection();
            preProtocol();
            criticalSection();
            postProtocol();
        }
    }

    public void nonCriticalSection() {
//       System.out.println ("2 ncc: Entering non-critical section" ) ;
      Time.delay(rnd.nextInt(120));
//       System.out.println ("2 ncc: Leaving non-critical section" ) ;
    }

    public void preProtocol() {
       Dekkers.c2 = 0 ;
       while (Dekkers.c1 != 1) 
          if (Dekkers.turn == 1) {
             Dekkers.c2 = 1 ;
             while (Dekkers.turn !=2) {
                System.out.println ("2 prep: Waiting for turn");
              }
              Dekkers.c2 = 0 ;
           }
       
    }

    public void criticalSection() {
       System.out.println("2 cs: Entering critical section ");
       Time.delay(rnd.nextInt(120));
       System.out.println("2 cs: Leaving critical section ");
    }

    public void postProtocol() {
       System.out.println("2 cs: Entering postProtocol section ");
       Dekkers.turn = 1;
       Time.delay(rnd.nextInt(120));
       Dekkers.c2 = 1 ;
       System.out.println("2 cs: Leaving postProtocol section ");
    }
}
