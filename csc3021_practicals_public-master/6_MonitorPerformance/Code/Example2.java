/*===========================================================
 * (C) Hans Vandierendonck, 2013
 *===========================================================*/
import java.util.concurrent.*;
import java.util.Random;

interface Queue {
    public void put(int i);
    public int get();
};

class Queue1 implements Queue {
    final int size;
    int[] buf;
    int head, tail, num;

    Queue1( int size_ ) {
	size = size_;
	buf = new int[size];
	head = tail = 0;
	num = 0;
    }

    public synchronized void put( int i ) {
	    // Wait while buffer is full...
	    while( num == size ) {
		try { wait(); }
		catch( InterruptedException e ) { }
	    }
	    buf[tail % size] = i;
	    ++tail;
	    ++num;
	    notifyAll();
	}

    public synchronized int get() {
	    int i;
	    // Wait while buffer is empty...
	    while( num == 0 ) {
		try { wait(); }
		catch( InterruptedException e ) { }
	    }
	    i = buf[head % size];
	    ++head;
	    --num;
	    notifyAll();
	    return i;
	}
}

class SPSCQueue implements Queue {
    final int size;
    int[] buf;
    volatile int head, tail;

    SPSCQueue( int size_ ) {
	size = size_;
	buf = new int[size];
	head = tail = 0;
    }

    public void put( int i ) {
	// Busy wait while buffer is full...
	// Tip: compare performance with and without the yield() statements.
	while( head % size == (tail+1) % size ) { Thread.yield(); }
	buf[tail % size] = i;
	++tail;
    }

    public int get() {
	int i;
	// Busy wait while buffer is empty...
	while( head == tail ) { Thread.yield(); }
	i = buf[head % size];
	++head;
	return i;
    }
}

class TestProcess extends Thread {
    private boolean is_producer;
    private volatile boolean stopped;
    private volatile boolean rdvz;
    private Queue queue;
    private int num_warmup;
    private int num_values;
    private CyclicBarrier barrier;

    TestProcess( boolean is_producer_, int num_warmup_, int num_values_,
		 Queue queue_, CyclicBarrier barrier_ ) {
	is_producer = is_producer_;
	stopped = rdvz = false;
	num_warmup = num_warmup_;
	num_values = num_values_;
	queue = queue_;
	barrier = barrier_;
    }

    public void run() {
	if( is_producer ) {
	    for( int v=0; v < num_warmup; ++v ) {
		queue.put( v );
	    }
	    barrier();
	    for( int v=0; v < num_values; ++v ) {
		queue.put( v );
	    }
	} else {
	    for( int v=0; v < num_warmup; ++v ) {
		queue.get();
	    }
	    barrier();
	    for( int v=0; v < num_values; ++v ) {
		queue.get();
	    }
	}
    }

    private void barrier() {
	try {
	    barrier.await();
	} catch( InterruptedException ex ) {
	} catch( BrokenBarrierException ex ) {
	}
    }
}

class Example2 {
    public static int parse_integer( String arg, int pos ) {
	int i = 0;
	try {
	    i = Integer.parseInt( arg );
	} catch( NumberFormatException e ) {
	    System.err.println( "Argument " + pos + "'" + arg
				+ "' must be an integer" );
	}
	return i;
    }

    public static double parse_double( String arg, int pos ) {
	double i = 0;
	try {
	    i = Double.parseDouble( arg );
	} catch( NumberFormatException e ) {
	    System.err.println( "Argument " + pos + "'" + arg
				+ "' must be a double" );
	}
	return i;
    }

    public static void main (String[] args) {
	if( args.length < 5 ) {
	    System.err.println("Usage: Example2 <num_producers> <num_consumers> <buffer_size> <type> <measure-elms>");
	    System.err.println("       type=1: Queue monitor");
	    System.err.println("       type=2: Single-Producer Single-Consumer Queue monitor");
	    System.exit( 1 );
	}

	int nprod = parse_integer( args[0], 1 );
	int ncons = parse_integer( args[1], 2 );
	int bufsz = parse_integer( args[2], 3 );
	int type = parse_integer( args[3], 4 );
	int num_measured = parse_integer( args[4], 5 );
	int num_warmup = num_measured / 2;

	if( type == 2 && ( nprod != 1 || ncons != 1 ) ) {
	    System.err.println( "Error: when type=2 (SPSC), set nprod=ncons=1");
	    System.exit( 1 );
	}

        System.out.println("Measuring performance with " + nprod
			   + " producers and " + ncons
			   + " consumers on a buffer of " + bufsz
			   + " entries.");

	int nprocesses = nprod + ncons;

	Queue queue = null;
	if( type == 1 )
	    queue = new Queue1( bufsz );
	else if( type == 2 )
	    queue = new SPSCQueue( bufsz );
	else {
	    System.err.println( "Bad value for type, must be 1 or 2" );
	    System.exit( 1 );
	}

	final CyclicBarrier barrier = new CyclicBarrier( nprocesses+1,
							 new Runnable() {
							     public void run() {
								 System.gc();
							     }
							 } );

	TestProcess[] processes = new TestProcess[nprocesses];

	// Create all of the threads
	int wdone = 0, mdone = 0;
	for( int i=0; i < nprod; ++i ) {
	    int nwarm;
	    int nmeas;
	    if( i < nprod-1 ) {
		nwarm = num_warmup/nprod;
		nmeas = num_measured/nprod;
		wdone += nwarm;
		mdone += nmeas;
	    } else {
		nwarm = num_warmup - wdone;
		nmeas = num_measured - mdone;
	    }
	    processes[i] = new TestProcess( true, nwarm, nmeas,
					    queue, barrier );
	}
	wdone = mdone = 0;
	for( int i=0; i < ncons; ++i ) {
	    int nwarm;
	    int nmeas;
	    if( i < ncons-1 ) {
		nwarm = num_warmup/ncons;
		nmeas = num_measured/ncons;
		wdone += nwarm;
		mdone += nmeas;
	    } else {
		nwarm = num_warmup - wdone;
		nmeas = num_measured - mdone;
	    }
	    processes[nprod+i] = new TestProcess( false, nwarm, nmeas,
						  queue, barrier );
	}

	// Start all of the threads and let them warmup.
	// Warming up is important to let the JIT do it's work (i.e.,
	// compile and optimize the code to make ti faster). When the JIT
	// kicks in, measured performance numbers are distorted and unreliable.
	for( int i=0; i < nprocesses; ++i ) {
	    processes[i].start();
	}

	// The garbage collector is another source of disruption to performance.
	// That's why will call the GC explicitly when reaching the barrier
	// (see barrier creation). You would normally never call the GC
	// directly.
	try {
	    barrier.await();
	} catch( InterruptedException ex ) {
	} catch( BrokenBarrierException ex ) {
	}

	// Time we start (somewhere after the rendez-vous)
	long start_time = System.nanoTime();

	// Join threads (so we know they are really done).
	for( int i=0; i < nprocesses; ++i ) {
	    try { processes[i].join(); } catch( InterruptedException e ) { }
	}

	// Time we stopped
	long stop_time = System.nanoTime();
	long delay = stop_time - start_time; // nanoseconds

	// Get the results out.
	float f_delay = ((float)1.e-9)*((float)delay);
	float rate = ((float)(2*num_measured)) / f_delay;

	System.out.println( "Over a period of " + f_delay
			    + " seconds, performed " + num_measured
			    + " put and get operations." );
	System.out.println( "Average operations/sec: " + rate );
    }
}
