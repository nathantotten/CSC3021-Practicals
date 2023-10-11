/*===========================================================
	CSC206 - Bouncing ball demo.
	
	This is a sequential program which animates ONE bouncing ball
	
	
===========================================================*/
import java.awt.*;

class BouncingBall {
	public static void main(String argv[]) {
		final int width = 200;		// Width of rectangle
		final int height = 200;		// Height of rectangle
		Rectangle bounds = new Rectangle(0, 30, width,height);	// Rectangle for ball to bounce in
		
		AppWindow window = new AppWindow("Bouncing ball", width, height); // Set up the window
		Graphics graphics = window.getGraphics(); 											

		Ball ball = new Ball(graphics, bounds, 10);	// Create a ball object												
		ball.animate();															
	}
}


/*===========================================================
	Ball class
===========================================================*/
class Ball{
	Graphics graphics;	// graphics area to draw the ball in
	Rectangle bounds;		// area that the ball is allowed to boucne in
	float xPosition;		// x position on the ball
 	float	yPosition;		// y position of the ball
	float xVelocity;		// horizontal velocity of the ball
	float yVelocity;		// vertical velocity of the ball
	int size;						// size of the ball
	
	/*===========================================================
		Constructor for the ball object
	===========================================================*/
	Ball (Graphics graphics, Rectangle bounds, int size) {
		this.graphics = graphics;
		this.bounds = bounds;
		this.xPosition = (bounds.x + bounds.width)/2;				// Center of rectangle
 		this.yPosition = (bounds.y + bounds.height)/2;		
		this.xVelocity = (float) (10 - (Math.random()*20));	// Random horizintal velocity
		this.yVelocity = (float) (10 - (Math.random()*20));	// Random vertical velocity
		this.size = size;
	}
	
	/*===========================================================
		Calculates the new position of the ball
	===========================================================*/
	void move() {
			// Add velocity to y position to get new position
			yPosition = yPosition + yVelocity;
			
			//check for collision with top edge 
			if (yPosition  < bounds.y && yVelocity < 0) {
				yVelocity = -yVelocity; // changes direction
			}
			
			// Check for collision with bottom edge
			else if ((yPosition + size) > (bounds.y + bounds.height) && yVelocity > 0) {
				yVelocity = -yVelocity; // changes direction
				yPosition -= 2 * ((yPosition + size) - (bounds.y + bounds.height));
			}
			
			// Add velocity to x position to get new position
			xPosition = xPosition + xVelocity;

			// Checks to see if the x position is out of bounds
			if (xPosition < bounds.x || (xPosition + size)>(bounds.x + bounds.width)) {
				xVelocity = -xVelocity; // changes direction
				if (xPosition < bounds.x) {
					xPosition -= 2*(xPosition-bounds.x);
				}
				else {
					xPosition -= 2*((xPosition + size)-(bounds.x + bounds.width));
				}
			}
	}

	/*===========================================================
		Draw the ball
	===========================================================*/
	void draw(Color color) {
		graphics.setColor(color);
		graphics.fillOval( (int) xPosition, (int) yPosition, size, size);
	}
	
	/*===========================================================
		Moves the ball and then updates the display
	===========================================================*/
	public void animate() {
			while (true) {
				draw(Color.white);			// Clear previous position		
				move();									// Move the ball	
				draw(Color.black);			// Draw ball in new position
				Time.delay(50);					// Wait 50 milliseconds
			}
	}
}
