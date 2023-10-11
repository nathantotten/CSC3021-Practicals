/*===========================================================
    Java version of the second mutal exclusion solution.

    This algorithm does not guarantee mutual exclusion.

    G McClements 30/6/99; PLK February 2000; NSS July 2001;
    HV September 2014.
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

class Attempt2 {

    public static volatile int c1 = 1;
    public static volatile int c2 = 1;

    public static void main (String[] args) {

        P1 thread1 = new P1();
        P2 thread2 = new P2();

        thread1.start();
        thread2.start();
    }
}

class P1 extends Thread {
    Random rnd = new Random();

    public void run() {
        while (true) {
            nonCriticalSection();
            preProtocol();
            criticalSection();
            postProtocol();
       }
    }

    public void nonCriticalSection() {
    }

    public void preProtocol() {
       while (Attempt2.c2 != 1) { Thread.yield(); }
       Time.delay(rnd.nextInt(120));
       Attempt2.c1 = 0;
    }

    public void criticalSection() {
       System.out.println("1 cs: Entering critical section ");
       System.out.println("1 cs: In critical section ");
       Time.delay(rnd.nextInt(120));
       System.out.println("1 cs: Leaving critical section ");
    }

    public void postProtocol() {
       Attempt2.c1 = 1;
       Time.delay(rnd.nextInt(120));
    }
}

class P2 extends Thread {
    Random rnd = new Random();

    public void run() {
        while (true) {
            nonCriticalSection();
            preProtocol();
            criticalSection();
            postProtocol();
    }
    }

    public void nonCriticalSection() {
    }

    public void preProtocol() {
       while (Attempt2.c1 != 1) { Thread.yield(); }
       Time.delay(rnd.nextInt(120));
       Attempt2.c2 = 0;
    }

    public void criticalSection() {
       System.out.println("2 cs: Entering critical section");
       System.out.println("2 cs: In critical section");
       Time.delay(rnd.nextInt(120));
       System.out.println("2 cs: Leaving critical section");
    }

    public void postProtocol() {
       Attempt2.c2 = 1;
       Time.delay(rnd.nextInt(120));
    }
}
