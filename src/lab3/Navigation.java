package lab3;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Navigation extends Thread {

	double x, y, xTarget, yTarget;

	Odometer odometer;

	public Navigation(Odometer odometer) {
		this.odometer = odometer;
	}

	private static final long NAVIGATION_PERIOD = 10;
	static final double leftRadius = 2.1;
	static final double rightRadius = 2.1;
	static final double wheelBase = 15.4;
	static final double errorThreshold = 5.0;

	private static final int ROTATE_SPEED = 150;

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	// COORDINATES
	Coordinate coord1 = new Coordinate(60, 30);
	Coordinate coord2 = new Coordinate(30, 30);
	Coordinate coord3 = new Coordinate(30, 60);
	Coordinate coord4 = new Coordinate(60, 0);

	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			x = odometer.getX();
			y = odometer.getY();

			// TODO do not setTarget as frequently
			if (coord4.getIsVisited()) {
				leftMotor.stop();
				rightMotor.stop();
			}

			else if (coord3.getIsVisited()) {
				setTarget(coord4);
				coord4.setIsVisited(x, y);
			}

			else if (coord2.getIsVisited()) {
				setTarget(coord3);
				coord3.setIsVisited(x, y);
			}

			else if (coord1.getIsVisited()) {
				setTarget(coord2);
				coord2.setIsVisited(x, y);
			}

			else {
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

		// Calculate minimal angle for theta
		double xDiff = xTarget - this.x;
		double yDiff = yTarget - this.y;
		double theta = Math.toDegrees(Math.atan2(yDiff, xDiff));

		if (Math.abs(odometer.getTheta() - theta) > errorThreshold) {
			turnTo(theta);
		}

		else {
			leftMotor.forward();
			rightMotor.forward();
		}
	}

	public void turnTo(double targetTheta) {
		double currentTheta = odometer.getTheta();
		double rotateAmount = currentTheta - targetTheta;

		if (currentTheta - targetTheta > 180) {
			rotateAmount = 360 - (currentTheta - targetTheta);
		}

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(leftRadius, wheelBase, rotateAmount), true);
		rightMotor.rotate(-convertAngle(rightRadius, wheelBase, rotateAmount), false);

		if (currentTheta > 180) {
			odometer.setTheta(currentTheta - 360);
		}

		if (currentTheta < -180) {
			odometer.setTheta(currentTheta + 360);
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
