package lab4;
/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

/*
 * File: Navigation.java
 * Written by: Sean Lawlor and Takeshi Musgrave, Keith MacKinnon
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.nxt.NXTRegulatedMotor;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 4000;
	final static double DEG_ERR = 2.0, CM_ERR = 0.5;
	private Odometer odometer;
	private NXTRegulatedMotor leftMotor, rightMotor;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		NXTRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm
	 * Will travel to designated position, while constantly updating it's
	 * heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR
				|| Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX()))
					* (180.0 / Math.PI);
			minAng = 90 - minAng;
			if (minAng < 0)
				minAng += 360;
			this.turnTo(minAng, false);
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean
	 * controls whether or not to stop the motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getTheta();

		int decider;

		if (error < -180.0) {
			decider = 0; // CW
		} else if (error < 0.0) {
			decider = 1; // CCW
		} else if (error > 180.0) {
			decider = 2; // CCW
		} else {
			decider = 3; // CW
		}

		while (Math.abs(error) > DEG_ERR) {
			error = angle - this.odometer.getTheta();
			switch (decider) {
			case 0:
				this.setSpeeds(SLOW, -SLOW);// CW
				break;
			case 1:
				this.setSpeeds(-SLOW, SLOW); // CCW
				break;
			case 2:
				this.setSpeeds(-SLOW, SLOW); // CCW
				break;
			case 3:
				this.setSpeeds(SLOW, -SLOW); // CW
				break;
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}

	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getTheta()))
				* distance, Math.cos(Math.toRadians(this.odometer.getTheta()))
				* distance);

	}
}
