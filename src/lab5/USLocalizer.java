package lab5;

/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import lejos.nxt.UltrasonicSensor;

public class USLocalizer {
	public enum LocalizationType {
		FALLING_EDGE, RISING_EDGE
	};

	public final static double ROTATION_SPEED = 30;
	public final static long TIME_PERIOD = 50; // delay to getFilteredData()

	private Odometer odo;
	private TwoWheeledRobot robot;
	private UltrasonicSensor us;
	private LocalizationType locType;
	private Navigation nav;

	private int currentDistance; // distance read by getFilteredData()
	private double angleA = 0; // angle of right wall (assuming facing away)
	private double angleB = 0; // angle of left wall

	private int count255 = 0; // number of consecutive max readings
	private int countWall = 0; // number of consecutive wall readings

	public USLocalizer(Odometer odo, UltrasonicSensor us,
			LocalizationType locType) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.us = us;
		this.locType = locType;
		this.nav = new Navigation(this.odo);
	}

	public void doLocalization() {

		boolean isFacingWall; // just a check to determine orientation
		double deltaTheta; // used to determine starting angle

		// determine initial position of looking at or away from wall
		for (int i = 0; i < 5; i++) {
			if (getFilteredData() <= 50) {
				countWall++;
			}
		}

		isFacingWall = countWall > 3; // either facing wall or away from wall

		if (locType == LocalizationType.FALLING_EDGE) {
			// robot begins facing away from the wall
			if (!isFacingWall) {
				fallingEdge();

			} else { // robot starts by facing the wall
				// only begin falling edge routine once facing away from wall
				while (count255 < 5) {
					robot.setRotationSpeed(ROTATION_SPEED);
					if (getFilteredData() == 255) {
						count255++;
					}
				}
				fallingEdge();
			}
			// to stop the rotation
			robot.setRotationSpeed(0);

			// calculates the corrected angle
			if (angleB > angleA) {
				deltaTheta = 225 - ((angleA + angleB) / 2);
			} else {
				deltaTheta = 45 - ((angleA + angleB) / 2);
			}

			// update the odometer position (example to follow:)
			odo.setPosition(new double[] { 0.0, 0.0, deltaTheta + angleB },
					new boolean[] { true, true, true });
		} else {
			countWall = 0;

			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall. This
			 * is very similar to the FALLING_EDGE routine, but the robot will
			 * face toward the wall for most of it.
			 */

			if (isFacingWall) {
				risingEdge();
			} else {
				// only begin rising edge routine once facing wall
				while (countWall < 5) {
					robot.setRotationSpeed(ROTATION_SPEED);
					if (getFilteredData() < 30) {
						countWall++;
					}
				}
				risingEdge();
			}

			robot.setRotationSpeed(0);

			// calculates the corrected angle
			if (angleA > angleB) {
				deltaTheta = 225 - ((angleA + angleB) / 2); 
			} else {
				deltaTheta = 45 - ((angleA + angleB) / 2); 
			}

			// update the odometer position (example to follow:)
			odo.setPosition(new double[] { 0.0, 0.0, deltaTheta + angleB },
					new boolean[] { true, true, true });

		}

		// find x and y positions based on perpendicular distances from walls
		nav.turnTo(180, true);
		currentDistance = getFilteredData();
		odo.setY(currentDistance - 25); // minus 30 + 7

		nav.turnTo(270, true);
		currentDistance = getFilteredData();
		odo.setX(currentDistance - 25); // minus 30 + 5

		// go to a position near 0,0 which allows for light localization
		nav.travelTo(0, 0);
		nav.turnTo(0, true); // to turn to a heading of 0 degrees
	}

	public int getCurrentDistance() {
		return currentDistance;
	}

	public double getAngleA() {
		return angleA;
	}

	public double getAngleB() {
		return angleB;
	}

	public void fallingEdge() {
		boolean isLatched = false; // whether angle is recorded

		// head to right wall
		while (!isLatched) {
			robot.setRotationSpeed(ROTATION_SPEED);
			currentDistance = getFilteredData();

			// right wall detected
			if (currentDistance < 30) {
				angleA = odo.getTheta(); // latch angle
				isLatched = true;
				break;
			}
		}

		// to reset isLatched
		while (isLatched) {
			robot.setRotationSpeed(-ROTATION_SPEED);
			currentDistance = getFilteredData();

			// ensure facing away from walls before attempting to detect angles
			if (currentDistance == 255) {
				count255++;
			} else {
				count255 = 0;
			}

			// now ready to detect left wall
			if (count255 >= 5) {
				isLatched = false;
			}
		}

		// head to left wall
		while (!isLatched) {
			robot.setRotationSpeed(-ROTATION_SPEED);
			currentDistance = getFilteredData();

			// left wall detected
			if (currentDistance < 30) {
				angleB = odo.getTheta(); // latch angle
				break;
			}
		}
	}

	public void risingEdge() {
		boolean isLatched = false; // whether angle is recorded

		// turn clockwise to detect open space
		while (!isLatched) {
			robot.setRotationSpeed(ROTATION_SPEED);
			currentDistance = getFilteredData();

			// open space detected
			if (currentDistance > 50) {
				angleB = odo.getTheta(); // latch angle
				isLatched = true;
				break;
			}
		}

		// to reset isLatched
		while (isLatched) {
			robot.setRotationSpeed(-ROTATION_SPEED);
			currentDistance = getFilteredData();

			// ensure facing wall before attempting to detect open space again
			if (currentDistance < 30) {
				countWall++;
			} else {
				countWall = 0;
			}

			// now ready to detect open space
			if (countWall >= 5) {
				isLatched = false;
			}
		}

		// turn counter clockwise to detect open space
		while (!isLatched) {
			robot.setRotationSpeed(-ROTATION_SPEED);
			currentDistance = getFilteredData();

			// open space detected
			if (currentDistance > 50) {
				angleA = odo.getTheta(); // latch angle
				break;
			}
		}
	}

	private int getFilteredData() {
		int distance;

		// do a ping
		us.ping();

		// wait for the ping to complete
		try {
			Thread.sleep(TIME_PERIOD);
		} catch (InterruptedException e) {
		}

		// there will be a delay here
		distance = us.getDistance();

		return distance;
	}
}
