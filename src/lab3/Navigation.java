package lab3;

/* Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Group 26
 */

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Navigation extends Thread {

	double x, y, xTarget, yTarget; // current and target positions

	Odometer odometer; // declare an Odometer object

	// Class constants
	private static final long NAVIGATION_PERIOD = 25;
	private static final double LEFT_RADIUS = 2.1; // left wheel radius (cm)
	private static final double RIGHT_RADIUS = 2.1; // right wheel radius (cm)
	private static final double WHEEL_BASE = 15.5; // wheel track (cm)
	private static final double ERROR_THRESHOLD = 1; // measured in degrees

	private static final int ROTATE_SPEED = 180; // big turns
	private static final int FORWARD_SPEED = 360; // normal driving
	private static final int TURNING_SPEED = 240; // adjusting position

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	// COORDINATES
	Coordinate coord0 = new Coordinate(0, 0); // starting position
	Coordinate coord1 = new Coordinate(60, 30);
	Coordinate coord2 = new Coordinate(30, 30);
	Coordinate coord3 = new Coordinate(30, 60);
	Coordinate coord4 = new Coordinate(60, 0); // ending position

	public Navigation(Odometer odometer) {
		this.odometer = odometer;
	}

	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			x = odometer.getX();
			y = odometer.getY();
			double currentTheta = odometer.getTheta();

			// we want theta in the range of -180 to 180
			if (currentTheta > 180) {
				odometer.setTheta(currentTheta - 360);
			} else if (currentTheta < -180) {
				odometer.setTheta(currentTheta + 360);
			}

			// determines which coordinate to target
			if (coord4.getIsVisited()) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			} else if (coord3.getIsVisited()) {
				setTarget(coord4);
				coord4.setIsVisited(x, y);
			} else if (coord2.getIsVisited()) {
				setTarget(coord3);
				coord3.setIsVisited(x, y);
			} else if (coord1.getIsVisited()) {
				setTarget(coord2);
				coord2.setIsVisited(x, y);
			} else {
				setTarget(coord1);
				coord1.setIsVisited(x, y);
			}

			travelTo(xTarget, yTarget); // travel to the target set just above

			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < NAVIGATION_PERIOD) {
				try {
					Thread.sleep(NAVIGATION_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	public void travelTo(double xTarget, double yTarget) {

		// Determine whether to turn or not
		double xDiff = xTarget - this.x;
		double yDiff = yTarget - this.y;
		double targetTheta = Math.toDegrees(Math.atan2(yDiff, xDiff));

		// change in theta is target minus current
		double deltaTheta = targetTheta - odometer.getTheta();

		// if the heading is off by more than acceptable error, we must correct
		if (Math.abs(deltaTheta) > ERROR_THRESHOLD) {
			turnTo(targetTheta);
		} else {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
		}
	}

	public void turnTo(double targetTheta) {
		double currentTheta = odometer.getTheta();
		double rotate = targetTheta - currentTheta;

		// If at a point, turn to in place
		if (coord0.isAtPoint(x, y) || coord1.isAtPoint(x, y)
				|| coord2.isAtPoint(x, y) || coord3.isAtPoint(x, y)
				|| coord4.isAtPoint(x, y)) {

			// turn a minimal angle
			if (rotate > 180) {
				rotate -= 360;
			} else if (rotate < -180) {
				rotate += 360;
			}

			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.rotate(-convertAngle(LEFT_RADIUS, WHEEL_BASE, rotate),
					true);
			rightMotor.rotate(convertAngle(RIGHT_RADIUS, WHEEL_BASE, rotate),
					false);
		}
		// turn while travelling by adjusting motor speeds
		else if (rotate > 0) {
			leftMotor.setSpeed(TURNING_SPEED); // correct left
			rightMotor.setSpeed(FORWARD_SPEED);

		} else if (rotate < 0) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(TURNING_SPEED); // correct right
		}
	}

	// return true if another thread has called travelTo() or turnTo()
	// and has yet to return
	public boolean isNavigating() {
		return false;
	}

	// gets the x and y targets from the specified coordinates
	public void setTarget(Coordinate coord) {
		xTarget = coord.getX();
		yTarget = coord.getY();
	}

	// returns the number of degrees the wheels must turn over a distance
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// returns the number of degrees to turn a certain angle
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
