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

class ResultAggregator  {
    private int num_values;
    private int num_rounds;
    private int round;
    private final CyclicBarrier barrier;
    private long sum_rate;
    private long sum_rate_sq;
    private double sum_delay;
    private long last_time;

    ResultAggregator( int nv, int nr, int np ) {
	num_values = nv;
	num_rounds = nr;
	round = 0;
	sum_rate = sum_rate_sq = 0;
	sum_delay = 0;
	last_time = System.nanoTime();

	barrier = new CyclicBarrier( np,
				     new Runnable() {
					 public void run() {
					     long now = System.nanoTime();
					     long delay = now - last_time;
					     double f_delay = (double)delay * 1e-9;
					     if( round > 0 ) { // skip 1st round
						 double r = (double)num_values / f_delay;
						 sum_rate += r;
						 sum_rate_sq += r*r;
						 sum_delay += f_delay;
					     }
					     round++;
					     if( round > 1 ) {
						 System.out.println( "Delay is " + f_delay + " secs" );
					     } else {
						 System.out.println( "delay is " + f_delay + " secs (warmup)" );
					     }
					     System.gc();
					     System.gc();
					     System.gc();
					     last_time = System.nanoTime();
					 }
				     } );
    }

    public boolean isFinished() {
	return round >= num_rounds;
    }

    public double getAvgDelay() {
	return (double)sum_delay / (double)(num_rounds-1);
    }

    public double getAvgRate() {
	return (double)sum_rate / (double)(num_rounds-1);
    }

    public double getStdDevRate() {
	double s1 = (double)sum_rate;
	double s2 = (double)sum_rate_sq;
	double N = (double)(num_rounds-1);
	return Math.sqrt( (N*s2-s1*s1)/(N*(N-1)) );
    }

    public void syncBarrier() {
	try {
	    barrier.await();
	} catch( InterruptedException ex ) {
	} catch( BrokenBarrierException ex ) {
	}
    }
}

class TestProcess extends Thread {
    private boolean is_producer;
    private volatile boolean stopped;
    private volatile boolean rdvz;
    private Queue queue;
    private int num_values;
    private ResultAggregator agg;

    TestProcess( boolean is_producer_, int num_values_,
		 Queue queue_, ResultAggregator agg_ ) {
	is_producer = is_producer_;
	stopped = rdvz = false;
	num_values = num_values_;
	queue = queue_;
	agg = agg_;
    }

    public void run() {
	if( is_producer ) {
	    while( !agg.isFinished() ) {
		for( int v=0; v < num_values; ++v ) {
		    queue.put( v );
		}
		agg.syncBarrier();
	    }
	} else {
	    while( !agg.isFinished() ) {
		for( int v=0; v < num_values; ++v ) {
		    queue.get();
		}
		agg.syncBarrier();
	    }
	}
    }
}

class Example3 {
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
	if( args.length < 6 ) {
	    System.err.println("Usage: Example3 <num_producers> <num_consumers> <buffer_size> <type> <measure-elms> <rounds>");
	    System.err.println("       type=1: Queue monitor");
	    System.err.println("       type=2: Single-Producer Single-Consumer Queue monitor");
	    System.exit( 1 );
	}

	int nprod = parse_integer( args[0], 1 );
	int ncons = parse_integer( args[1], 2 );
	int bufsz = parse_integer( args[2], 3 );
	int type = parse_integer( args[3], 4 );
	int num_measured = parse_integer( args[4], 5 );
	int num_rounds = parse_integer( args[5], 6 ) + 1;

	if( type == 2 && ( nprod != 1 || ncons != 1 ) ) {
	    System.err.println( "Error: when type=2 (SPSC), set nprod=ncons=1");
	    System.exit( 1 );
	}
	if( num_rounds <= 1 ) {
	    System.err.println( "Error: num_rounds should be at least 1");
	    System.exit( 1 );
	}

        System.out.println("Measuring performance with " + nprod
			   + " producers and " + ncons
			   + " consumers on a buffer of " + bufsz
			   + " entries. Doing " + (num_rounds-1)
			   + " rounds of the experiment after a warmup"
			   + " round and " + num_measured
			   + " put and get operations per round.");

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

	ResultAggregator agg
	    = new ResultAggregator( num_measured, num_rounds, nprocesses );

	TestProcess[] processes = new TestProcess[nprocesses];

	// Create all of the threads
	int mdone = 0;
	for( int i=0; i < nprod; ++i ) {
	    int nmeas;
	    if( i < nprod-1 ) {
		nmeas = num_measured/nprod;
		mdone += nmeas;
	    } else {
		nmeas = num_measured - mdone;
	    }
	    processes[i] = new TestProcess( true, nmeas, queue, agg );
	}
	mdone = 0;
	for( int i=0; i < ncons; ++i ) {
	    int nmeas;
	    if( i < ncons-1 ) {
		nmeas = num_measured/ncons;
		mdone += nmeas;
	    } else {
		nmeas = num_measured - mdone;
	    }
	    processes[nprod+i] = new TestProcess( false, nmeas, queue, agg );
	}

	// Start all of the threads and let them warmup.
	// Warming up is important to let the JIT do it's work (i.e.,
	// compile and optimize the code to make ti faster). When the JIT
	// kicks in, measured performance numbers are distorted and unreliable.
	// The garbage collector is another source of disruption to performance.
	// That's why will call the GC explicitly when reaching the barrier
	// (see barrier creation). You would normally never call the GC
	// directly.
	for( int i=0; i < nprocesses; ++i ) {
	    processes[i].start();
	}

	// Join threads (cleanup properly).
	for( int i=0; i < nprocesses; ++i ) {
	    try { processes[i].join(); } catch( InterruptedException e ) { }
	}

	// Get the results out.
	double avg_delay = agg.getAvgDelay();
	double avg_rate = agg.getAvgRate();
	double sdv_rate = agg.getStdDevRate();

	System.out.println( "Average delay of a trial was " + avg_delay
			    + " seconds." );
	System.out.println( "Average operations/sec: " + avg_rate );
	System.out.println( "Standard deviation operations/sec: " + sdv_rate );
    }
}
