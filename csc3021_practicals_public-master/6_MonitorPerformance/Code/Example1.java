/*===========================================================
 * (C) Hans Vandierendonck, 2013
 *===========================================================*/
import java.util.concurrent.*;
import java.util.Random;

interface RW_lock {
    public void acquireRead();
    public void releaseRead();
    public void acquireWrite();
    public void releaseWrite();
};

class ReadersWriter1 implements RW_lock {
    private boolean writing;
    private int nreaders;

    ReadersWriter1() {
	writing = false;
	nreaders = 0;
    }

    public synchronized void acquireRead() {
	while( writing ) {
	    try { wait(); }
	    catch( InterruptedException e ) { }
	}
	++nreaders;
    }

    public synchronized void releaseRead() {
	--nreaders;
	notify();
    }

    public synchronized void acquireWrite() {
	while( writing || nreaders > 0 ) {
	    try { wait(); }
	    catch( InterruptedException e ) { }
	}
	++nreaders;
    }

    public synchronized void releaseWrite() {
	--nreaders;
	notify();
    }
};

class ReadersWriter2 implements RW_lock {
    private boolean writing;
    private int nreaders;

    ReadersWriter2() {
	writing = false;
	nreaders = 0;
    }

    public synchronized void acquireRead() {
	while( writing ) {
	    try { wait(); }
	    catch( InterruptedException e ) { }
	}
	++nreaders;
    }

    public synchronized void releaseRead() {
	--nreaders;
	if( nreaders == 0 )
	    notify();
    }

    public synchronized void acquireWrite() {
	while( writing || nreaders > 0 ) {
	    try { wait(); }
	    catch( InterruptedException e ) { }
	}
	++nreaders;
    }

    public synchronized void releaseWrite() {
	--nreaders;
	notifyAll();
    }
};


class TestProcess extends Thread {
    private double frac_readers;
    private boolean stopped;
    private RW_lock rw_lock;
    private long num_acquire_read;
    private long num_acquire_write;

    TestProcess( double frac_readers_, RW_lock rw_lock_ ) {
	frac_readers = frac_readers_;
	stopped = false;
	rw_lock = rw_lock_;
	num_acquire_read = 0;
	num_acquire_write = 0;
    }

    public void run() {
	Random rng = new Random();
	while( !stopped ) {
	    // try { Thread.sleep( 1 ); }
	    // catch( InterruptedException e ) { }
	    double r = rng.nextDouble();
	    if( r < frac_readers ) {
		rw_lock.acquireRead();
		// try { Thread.sleep( 1 ); }
		// catch( InterruptedException e ) { }
		++num_acquire_read;
		rw_lock.releaseRead();
	    } else {
		rw_lock.acquireWrite();
		// try { Thread.sleep( 1 ); }
		// catch( InterruptedException e ) { }
		++num_acquire_write;
		rw_lock.releaseWrite();
	    }
	}
    }

    public void please_stop() {
	stopped = true;
    }

    public long getNumReadLocks() {
	return num_acquire_read;
    }
    public long getNumWriteLocks() {
	return num_acquire_write;
    }
}

class Example1 {
    private static final int msec_measured_interval = 2000;

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
	if( args.length < 3 ) {
	    System.err.println("Usage: Example1 <num_readers> <num_writers>");
	    System.exit( 1 );
	}

	int nprocesses = parse_integer( args[0], 1 );
	double frac_readers = parse_double( args[1], 2 );
	int type = parse_integer( args[0], 3 );

        System.out.println("Measuring performance with " + nprocesses
			   + " processes and " + frac_readers
			   + " percentage of read acquires.");

	RW_lock rw_lock;
	if( type == 1 ) {
	    rw_lock = new ReadersWriter1();
	} else {
	    rw_lock = new ReadersWriter2();
	}

	TestProcess[] processes = new TestProcess[nprocesses];

	// Create all of the threads
	for( int i=0; i < nprocesses; ++i ) {
	    processes[i] = new TestProcess( frac_readers, rw_lock );
	}

	// Time we start
	long start_time = System.nanoTime();

	// Start all of the threads
	for( int i=0; i < nprocesses; ++i ) {
	    processes[i].start();
	}

	// Let all of the threads run some time
	try { Thread.sleep( msec_measured_interval ); }
	catch( InterruptedException e ) { }

	// Now stop the threads.
	for( int i=0; i < nprocesses; ++i ) {
	    processes[i].please_stop();
	}

	// And join them (so we know they are really done).
	for( int i=0; i < nprocesses; ++i ) {
	    try { processes[i].join(); } catch( InterruptedException e ) { }
	}

	// Time we stopped
	long stop_time = System.nanoTime();
	long delay = stop_time - start_time; // nanoseconds

	// Get the results out.
	long r_locks = 0;
	long w_locks = 0;
	for( int i=0; i < nprocesses; ++i ) {
	    r_locks += processes[i].getNumReadLocks();
	    w_locks += processes[i].getNumWriteLocks();
	}
	float f_delay = ((float)1.e-9)*((float)delay);
	float r_rate = ((float)r_locks) / f_delay;
	float w_rate = ((float)w_locks) / ((float)1.e-9*(float)delay);

	System.out.println( "Over a period of " + f_delay
			    + " seconds, acquired " + r_locks
			    + " read locks and " + w_locks
			    + " write locks." );
	System.out.println( "Average read locks/sec: " + r_rate
			    + " average write locks/sec: " + w_rate );
    }
}
