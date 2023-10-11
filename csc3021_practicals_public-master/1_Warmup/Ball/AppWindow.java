import java.awt.*;
import java.awt.event.*;

/*===========================================================
	A frame which handles the close event and kills all current
	threads.
===========================================================*/
public class AppWindow extends Frame{
	public AppWindow (
		String title, 	// the title of the window
		int width, 			// the width of the window
		int height			// the height of the window without the title bar
	) 
	{
		super(title);
		setSize(width , height+25 ); // Add 25 to take into account the title bar
		setResizable(false);
		addWindowListener(new AppWindowAdapter());
		setVisible(true);	
	}
	
	// Default constructor
	public AppWindow () {
		super();
		setSize(200,225 );  
		setResizable(false);
		addWindowListener(new AppWindowAdapter());
		setVisible(true);	
	}
	
}

class AppWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
			System.exit(0);		//Kill the java runtime, including any threads started
	}
}
