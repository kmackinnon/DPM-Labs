package lab3;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Navigation extends Thread {

	double x, y, xTarget, yTarget;

	static final double leftRadius = 2.1;
	static final double rightRadius = 2.1;
	static final double wheelBase = 15.4;

	Odometer odometer = new Odometer();

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	// COORDINATES
	Coordinate coord1 = new Coordinate(60, 30);
	Coordinate coord2 = new Coordinate(30, 30);
	Coordinate coord3 = new Coordinate(30, 60);
	Coordinate coord4 = new Coordinate(60, 0);

	public void run() {

		odometer.start();


		

		while (true) {

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

		}

	}

	public void travelTo(double x, double y) {
		// TODO Calculate minimal angle for theta
		double xDiff = xTarget - x;
		double yDiff = yTarget - y;
		double theta = Math.atan2(yDiff, xDiff);
		
		turnTo(theta);
		leftMotor.forward();
		rightMotor.forward();
	}

	public void turnTo(double theta) {
		leftMotor.rotate(convertAngle(leftRadius, wheelBase, theta), true);
		rightMotor.rotate(-convertAngle(rightRadius, wheelBase, theta), false);
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
