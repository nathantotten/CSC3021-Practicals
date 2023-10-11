/*===========================================================
  Java version of the first mutal exclusion solution.

  This solution has all of the desired properties except for
  correct behaviour in the absence of contention.

  (1)  Run the program and note that there is no interleaving of
  instructions in critical sections.
  (2)  Remove the comments in the methods and run the program
  again. Note that some instructions (which ones ?) from
  one process can be interleaved with instructions in the
  other process's critical section.
  (3)  Call the killMe method from thread1's nonCritcal
  section. Observe the starvation of thread2.

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

class Attempt1 {

	public static volatile int turn=1;

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
			try {
				nonCriticalSection();
				preProtocol();
				criticalSection();
				postProtocol();
			} catch (InterruptedException ex) {
				System.out.println("1   : thread 1 is dead");
				break;
			}
		}
	}

	public void nonCriticalSection() throws InterruptedException {
		//       System.out.println("1 nc: Entering nonCritical section");
		Time.delay(rnd.nextInt(20));

		try {
			// killMe();
		} catch (InterruptedException ex) {
			throw ex;
		}

		//       System.out.println("1 nc: Leaving nonCritical section");
	}

	public void killMe() throws InterruptedException {
		interrupt();
		throw new InterruptedException();
	}

	public void preProtocol() {
		//       System.out.println("1 prep: Entering preProtocol section");
		while (Attempt1.turn == 2) {
			Thread.yield();
		}
		Time.delay(rnd.nextInt(20));
		//       System.out.println("1 prep: Leaving preProtocol section");
	}

	public void criticalSection() {
		System.out.println("1 cs: Entering critical section");
		System.out.println("1 cs: In critical section");
		Time.delay(rnd.nextInt(20));
		System.out.println("1 cs: Leaving critical section");
	}

	public void postProtocol() {
		//       System.out.println("1 postp: Entering postProtocol section");
		Time.delay(rnd.nextInt(20));
		Attempt1.turn = 2;
		//       System.out.println("1 postp: Leaving postProtocol section");
	}
}

class P2 extends Thread {
	Random rnd = new Random();

	public void run() {
		while (!interrupted()) {
			nonCriticalSection();
			preProtocol();
			criticalSection();
			postProtocol();
		}
	}

	public void nonCriticalSection() {
		System.out.println("2 nc: Entering nonCritical section");
		Time.delay(rnd.nextInt(20));
		System.out.println("2 nc: Leaving nonCritical section");
	}

	public void preProtocol() {
		System.out.println("2 prep: Entering preProtocol section");
		while (Attempt1.turn == 1) {
			Thread.yield();
		}
		Time.delay(rnd.nextInt(20));
		System.out.println("2 prep: Leaving preProtocol section");
	}

	public void criticalSection() {
		System.out.println("2 cs: Entering critical section");
		Time.delay(rnd.nextInt(20));
		System.out.println("2 cs: In critical section");
		Time.delay(rnd.nextInt(20));
		System.out.println("2 cs: Leaving critical section");
	}

	public void postProtocol() {
		//       System.out.println("2 postp: Entering postProtocol section");
		Time.delay(rnd.nextInt(20));
		Attempt1.turn = 1;
		//       System.out.println("2 postp: Leaving postProtocol section");
	}
}
