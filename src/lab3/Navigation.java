package lab3;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Navigation extends Thread {

	double x, y, xTarget, yTarget;

	Odometer odometer;

	public Navigation(Odometer odometer) {
		this.odometer = odometer;
	}

	private static final long NAVIGATION_PERIOD = 25;
	static final double leftRadius = 2.1;
	static final double rightRadius = 2.1;
	static final double wheelBase = 15.5;
	static final double errorThreshold = 1;

	private static final int ROTATE_SPEED = 180;
	private static final int FORWARD_SPEED = 360;

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	// COORDINATES
	Coordinate coord0 = new Coordinate(0, 0);
	Coordinate coord1 = new Coordinate(60, 30);
	Coordinate coord2 = new Coordinate(30, 30);
	Coordinate coord3 = new Coordinate(30, 60);
	Coordinate coord4 = new Coordinate(60, 0);

	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			double currentTheta = odometer.getTheta();
			
			if (currentTheta > 180) {
				odometer.setTheta(currentTheta - 360);
			} else if (currentTheta < -180) {
				odometer.setTheta(currentTheta + 360);
			}
			
			x = odometer.getX();
			y = odometer.getY();

			// TODO do not setTarget as frequently
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

			travelTo(xTarget, yTarget);

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

		double deltaTheta = targetTheta - odometer.getTheta();

		// if the heading is off, we must correct
		if (Math.abs(deltaTheta) > errorThreshold) {
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
		double rotateAmount = targetTheta - currentTheta;

		// to keep the angle in the range of -180 to 180
	
		// If at a point, turn to in place
		if (coord0.isAtPoint(x, y) || coord1.isAtPoint(x, y)
				|| coord2.isAtPoint(x, y) || coord3.isAtPoint(x, y)
				|| coord4.isAtPoint(x, y)) {

			// turn a minimal angle
			if (rotateAmount > 180) {
				rotateAmount = rotateAmount - 360;
			} else if (rotateAmount < -180) {
				rotateAmount = rotateAmount + 360;
			}

			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.rotate(-convertAngle(leftRadius, wheelBase, rotateAmount), true);
			rightMotor.rotate(convertAngle(rightRadius, wheelBase, rotateAmount), false);
		}
		// turn while travelling
		else if (rotateAmount > 0) {
			leftMotor.setSpeed(FORWARD_SPEED - 120);
			rightMotor.setSpeed(FORWARD_SPEED);

		} else if (rotateAmount < 0) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED - 120);
		}
	}

	public boolean isNavigating() {
		// TODO
		return false;
	}

	public void setTarget(Coordinate coord) {
		xTarget = coord.getX();
		yTarget = coord.getY();
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
