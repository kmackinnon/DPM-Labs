package lab2;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

//import lejos.nxt.*;

/*
 * Odometer.java
 */

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	
	double leftRadius = 2.1;
	double rightRadius = 2.1;
	double distanceBetweenWheels = 15.57;
	
	int prevTachoLeft = leftMotor.getTachoCount();
	int prevTachoRight = rightMotor.getTachoCount();
	
	int currTachoLeft;
	int currTachoRight;
	int diffTachoLeft;
	int diffTachoRight;
	
	double leftWheelArc;
	double rightWheelArc;
	double changeInTheta;
	double arcLengthTravelled;
	
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 90.0; //to point along y axis
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		
		long updateStart, updateEnd;
		
		while (true) {
			updateStart = System.currentTimeMillis();
			// put (some of) your odometer code here
			
			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				currTachoLeft = leftMotor.getTachoCount();
				currTachoRight = rightMotor.getTachoCount();

				diffTachoLeft = currTachoLeft - prevTachoLeft; 
				diffTachoRight = currTachoRight - prevTachoRight;

				leftWheelArc = Math.toRadians(diffTachoLeft)*leftRadius;
				rightWheelArc = Math.toRadians(diffTachoRight) *rightRadius;
				changeInTheta = (rightWheelArc - leftWheelArc) / distanceBetweenWheels;

				arcLengthTravelled = (rightWheelArc + leftWheelArc) / 2;

				setX(x + arcLengthTravelled*Math.cos(Math.toRadians(theta + changeInTheta/2)));
				setY(y + arcLengthTravelled*Math.sin(Math.toRadians(theta + changeInTheta/2))); 

				setTheta(theta + Math.toDegrees(changeInTheta));

				prevTachoLeft = currTachoLeft;
				prevTachoRight = currTachoRight;
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}