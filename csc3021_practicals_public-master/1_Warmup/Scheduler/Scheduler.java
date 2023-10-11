
/*-----------------------------------------------------
This program illustrates the random nature of process
scheduling.

Run it several times and compare the output.
Insert a time delay into the loop, e.g. Time.delay(50),
and run it several times.
Replace the time delay with Thread.yield() and run it
several times.
--------------------------------------------------------*/
class Scheduler {
    public static void main (String args[]) {
	TestThread t1, t2, t3;

	t1 = new TestThread ("Thread 1");
	t2 = new TestThread ("Thread 2");
	t3 = new TestThread ("Thread 3");

	t1.start ();
	t2.start ();
	t3.start ();
    }
}

class TestThread extends Thread {
    String id;
    int count = 1;

    public TestThread (String s) {
	id = s;
    }

    public void run() {
        do {
            System.out.println(id);
            count = count + 1;
        }  while (count < 10);
    }
}
