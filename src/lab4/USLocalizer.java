package lab4;

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
	private Navigation nav;

	private int currentDistance;
	private double angleA = 0;
	private double angleB = 0;

	private int count255 = 0;
	private int countWall = 0;

	public USLocalizer(Odometer odo, UltrasonicSensor us,
			LocalizationType locType) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.us = us;
		this.locType = locType;
		this.nav = new Navigation(this.odo);

		// switch off the ultrasonic sensor
		us.off();
	}

	public void doLocalization() {

		boolean isFacingWall;

		double deltaTheta;

		for (int i = 0; i < 5; i++) {
			if (getFilteredData() <= 50) {
				countWall++;
			}
		}

		isFacingWall = countWall > 3; // either facing wall or away from wall

		if (locType == LocalizationType.FALLING_EDGE) {

			if (!isFacingWall) {
				// robot begins facing away from the wall
				fallingEdge();

			} else { // now robot starts by facing the wall
				while (count255 < 5) {
					robot.setRotationSpeed(ROTATION_SPEED);
					if (getFilteredData() == 255) {
						count255++;
					}
				}
				fallingEdge();
			}

			robot.setRotationSpeed(0);
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
			
			if(isFacingWall){
				risingEdge();
			}
			else{
				while (countWall < 5) {
					robot.setRotationSpeed(ROTATION_SPEED);
					if (getFilteredData() < 30) {
						countWall++;
					}
				}
				risingEdge();
			}
			
			robot.setRotationSpeed(0);
			if (angleA > angleB) {
				deltaTheta = 225 - ((angleA + angleB) / 2);
			} else {
				deltaTheta = 45 - ((angleA + angleB) / 2);
			}

			// update the odometer position (example to follow:)
			odo.setPosition(new double[] { 0.0, 0.0, deltaTheta + angleB },
					new boolean[] { true, true, true });
			
		}
		
		nav.turnTo(180, true);
		currentDistance = getFilteredData();
		odo.setY(currentDistance - 30 - 6);
		
		nav.turnTo(270, true);
		currentDistance = getFilteredData();
		odo.setX(currentDistance - 30 - 6);
		
		nav.travelTo(0,0);
		nav.turnTo(0,true);
		
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
		boolean isLatched = false;

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
				count255++;
			} else {
				count255 = 0;
			}

			if (count255 >= 5) {
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
	}

	
	public void risingEdge(){
		
		boolean isLatched = false;

		// head to right wall
		while (!isLatched) {
			robot.setRotationSpeed(ROTATION_SPEED);
			currentDistance = getFilteredData();

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

			if (currentDistance <30) {
				countWall++;
			} else {
				countWall = 0;
			}

			if (countWall >= 5) {
				isLatched = false; // because we haven't made it to right
									// wall
			}
		}

		// head to right wall
		while (!isLatched) {
			robot.setRotationSpeed(-ROTATION_SPEED);
			currentDistance = getFilteredData();

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
