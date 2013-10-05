package lab4;

import lejos.nxt.Button;
import lejos.nxt.UltrasonicSensor;

public class USLocalizer {
	public enum LocalizationType {
		FALLING_EDGE, RISING_EDGE
	};

	public final static double ROTATION_SPEED = 30;
	public final static long TIME_PERIOD = 50;
	public final static double CRITICAL_SLOPE = 5;

	private Odometer odo;
	private TwoWheeledRobot robot;
	private UltrasonicSensor us;
	private LocalizationType locType;

	private int currentDistance;
	private double angleA = 0;
	private double angleB = 0;

	private int countFarOrClose = 0;

	public USLocalizer(Odometer odo, UltrasonicSensor us,
			LocalizationType locType) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.us = us;
		this.locType = locType;

		// switch off the ultrasonic sensor
		us.off();
	}

	public void doLocalization() {
		boolean isLatched = false;

		double deltaTheta;

		// robot begins facing away from the wall
		if (locType == LocalizationType.FALLING_EDGE) {

			while (Button.waitForAnyPress() != Button.ID_ENTER)
				;

			// head to right wall
			while (!isLatched) {
				robot.setRotationSpeed(ROTATION_SPEED);
				currentDistance = getFilteredData();

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

				if (currentDistance == 255) {
					countFarOrClose++;
				}

				else {
					countFarOrClose = 0;
				}

				if (countFarOrClose >= 5) {
					isLatched = false; // because we haven't made it to right
										// wall
				}
			}

			// head to right wall
			while (!isLatched) {
				robot.setRotationSpeed(-ROTATION_SPEED);
				currentDistance = getFilteredData();

				if (currentDistance < 30) {
					angleB = odo.getTheta(); // latch angle
					break;
				}
			}

			robot.setRotationSpeed(0);
			deltaTheta = 225 - ((angleA + angleB) / 2);

			// update the odometer position (example to follow:)
			odo.setPosition(new double[] { 0.0, 0.0, deltaTheta + angleB },
					new boolean[] { true, true, true });
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall. This
			 * is very similar to the FALLING_EDGE routine, but the robot will
			 * face toward the wall for most of it.
			 */

			while (Button.waitForAnyPress() != Button.ID_ENTER)
				;

			// head to right wall
			while (!isLatched) {
				robot.setRotationSpeed(ROTATION_SPEED);
				currentDistance = getFilteredData();

				if (currentDistance > 30) {
					angleA = odo.getTheta(); // latch angle
					isLatched = true;
					break;
				}
			}

			// to reset isLatched
			while (isLatched) {
				robot.setRotationSpeed(-ROTATION_SPEED);
				currentDistance = getFilteredData();

				if (currentDistance < 30) {
					countFarOrClose++;
				}

				else {
					countFarOrClose = 0;
				}

				if (countFarOrClose >= 5) {
					isLatched = false; // because we haven't made it to right
										// wall
				}
			}

			// head to right wall
			while (!isLatched) {
				robot.setRotationSpeed(-ROTATION_SPEED);
				currentDistance = getFilteredData();

				if (currentDistance > 30) {
					angleB = odo.getTheta(); // latch angle
					break;
				}
			}

			robot.setRotationSpeed(0);
			deltaTheta = 225 - ((angleA + angleB) / 2);

			// update the odometer position (example to follow:)
			odo.setPosition(new double[] { 0.0, 0.0, deltaTheta + angleB },
					new boolean[] { true, true, true });
		}
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
