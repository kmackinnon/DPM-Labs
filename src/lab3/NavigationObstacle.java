package lab3;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class NavigationObstacle extends Thread {

	private static final SensorPort usPort = SensorPort.S4;
	UltrasonicSensor usSensor = new UltrasonicSensor(usPort);

	double x, y, xTarget, yTarget; // current and target positions

	double obstacleTheta; // theta when robot first sees obstacle

	private static int countDistances; // the number of times sensor reads 255
	private static final int MAX_255_COUNT = 20; // number of 255 readings

	boolean isNearObstacle = false;

	// Class constants
	private static final long NAVIGATION_PERIOD = 25;
	private static final double LEFT_RADIUS = 2.1; // cm
	private static final double RIGHT_RADIUS = 2.1; // cm
	private static final double WHEEL_BASE = 15.5; // cm
	private static final double ERROR_THRESHOLD = 1; // degrees

	private static final int ROTATE_SPEED = 180; // big turns
	private static final int FORWARD_SPEED = 360; // normal driving speed
	private static final int OFF_COURSE_SPEED = 80; // large correction needed
	private static final int ON_COURSE_SPEED = 220; // small correction needed

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	// COORDINATES
	Coordinate coord0 = new Coordinate(0, 0);
	Coordinate coord1 = new Coordinate(0, 60);
	Coordinate coord2 = new Coordinate(60, 0);

	Odometer odometer;

	public NavigationObstacle(Odometer odometer) {
		this.odometer = odometer;
	}

	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			x = odometer.getX();
			y = odometer.getY();
			double currentTheta = odometer.getTheta();

			// to keep theta in range of -180 to 180
			if (currentTheta > 180) {
				odometer.setTheta(currentTheta - 360);
			} else if (currentTheta < -180) {
				odometer.setTheta(currentTheta + 360);
			}

			// determines which coordinate to target
			if (coord2.getIsVisited()) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			} else if (coord1.getIsVisited()) {
				setTarget(coord2);
				coord2.setIsVisited(x, y);
			} else {
				setTarget(coord1);
				coord1.setIsVisited(x, y);
			}

			// if not near an obstacle, travel normally
			if (!isNearObstacle) {
				travelTo(xTarget, yTarget);
			} else {
				// otherwise, avoid obstacle
				moveAroundObstacle();
			}

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

		// if the heading is off, we must correct if outside allowable error
		if (Math.abs(deltaTheta) > ERROR_THRESHOLD) {
			turnTo(targetTheta);
		} else {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
		}

		// if there is an obstacle, set boolean to true
		if (usSensor.getDistance() < 20) {
			isNearObstacle = true;
			obstacleTheta = odometer.getTheta();
		}
	}

	// determines turning behaviour
	public void turnTo(double targetTheta) {
		double currentTheta = odometer.getTheta();
		double rotate = targetTheta - currentTheta;

		// If at a point, turn in place
		if (coord0.isAtPoint(x, y) || coord1.isAtPoint(x, y)
				|| coord2.isAtPoint(x, y)) {

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
			
		} else if (rotate > 15) {
			leftMotor.setSpeed(OFF_COURSE_SPEED); // turn left
			rightMotor.setSpeed(FORWARD_SPEED);
		} else if (rotate < -15) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(OFF_COURSE_SPEED); // turn right
		} else if (rotate > 0) {
			leftMotor.setSpeed(ON_COURSE_SPEED); // correct left
			rightMotor.setSpeed(FORWARD_SPEED);
		} else if (rotate < 0) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(ON_COURSE_SPEED); // correct right
		}
	}

	public void moveAroundObstacle() {
		int distance = usSensor.getDistance();

		// if near block, make a nearly in-place turn
		if (distance < 20 || odometer.getTheta() > (obstacleTheta - 75)) {
			rightMotor.setSpeed(10);
		}

		// if the max distance holds for a certain number of pings, go straight
		else if (distance >= 255 && countDistances <= MAX_255_COUNT) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			countDistances++;
			return;
		} else if (distance < 255) {
			countDistances = 0; // reset the counter when sensor not seeing max
								// distance
		} else {
			isNearObstacle = false;
		}
	}

	public boolean isNavigating() {
		// TODO
		return false;
	}

	// gets the x and y targets from the specified coordinates
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
