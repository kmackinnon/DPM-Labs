package lab4;
/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import lejos.nxt.ColorSensor;
import lejos.nxt.Sound;

public class LightLocalizer extends Thread {

	private final static double ROTATION_SPEED = 30; //motor speed while turning
	private final static long CORRECTION_PERIOD = 10; //refresh rate of thread

	private static final double NEAR_ZERO = 1; // to return to approx 0 deg
	private static final int THRESHOLD = 520; // to detect lines
	private static final int MIN_READINGS = 7; // lines detected only once
									
	private Odometer odo;
	private TwoWheeledRobot robot;
	private ColorSensor cs;
	private Navigation nav;

	public LightLocalizer(Odometer odo, ColorSensor cs) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.cs = cs;
		this.nav = new Navigation(this.odo);

		// turn on the light
		cs.setFloodlight(true);
	}

	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees

		long correctionStart, correctionEnd;

		int sensorCounter = 0;
		double x, y, thetaY, thetaX;
		double angle1 = -1; //initialized to impossible values
		double angle2 = -1;
		double angle3 = -1;
		double angle4 = -1;
		double d = 12; // distance between light sensor and wheel center

		while (true) {
			correctionStart = System.currentTimeMillis();

			robot.setRotationSpeed(-ROTATION_SPEED);

			if (cs.getRawLightValue() < THRESHOLD) { // to detect black lines
				sensorCounter++;

				// if the robot is crossing a line, get respective angles
				if (sensorCounter >= MIN_READINGS) {
					Sound.beep(); // to aid in debugging -- to test for lines
					if (angle1 == -1) {
						angle1 = odo.getTheta();
					} else if (angle2 == -1) {
						angle2 = odo.getTheta();
					} else if (angle3 == -1) {
						angle3 = odo.getTheta();
					} else if (angle4 == -1) {
						angle4 = odo.getTheta();
					}
					sensorCounter = 0; // reset after each line
				}
			}

			else {
				sensorCounter = 0; // reset in case of no black line
			}

			// once all angles have been calculated
			if (angle4 != -1 && Math.abs(odo.getTheta()) < NEAR_ZERO) {
				break;
			}

			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the localization will be
					// interrupted by another thread
				}
			}
		}

		robot.setRotationSpeed(0); //stop rotating once all lines detected

		// calculations to get theta x and y
		thetaY = Math.abs(angle1 - angle3);
		thetaX = 360 - Math.abs(angle4 - angle2);

		if (thetaY > 180) {
			thetaY = 360 - thetaY;
		}

		if (thetaX > 180) {
			thetaX = 360 - thetaX;
		}

		// calculate correct x and y positions
		x = -d * Math.cos(Math.toRadians(thetaY / 2));
		y = -d * Math.cos(Math.toRadians(thetaX / 2));

		// navigate to point (0,0) and then set heading to 0 degrees
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
	}

}
