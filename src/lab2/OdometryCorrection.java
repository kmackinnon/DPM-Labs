package lab2;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;

/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private ColorSensor sensor = new ColorSensor(SensorPort.S1); // created our
																	// light
																	// sensor
																	// object

	private int sensorCounter = 0;
	private double lastXValue = 0.0;
	private double lastYValue = 0.0;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		sensor.setFloodlight(true);
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {

			correctionStart = System.currentTimeMillis();

			// put your correction code here
			if (lightSensorReading() < 520) { // to detect black lines
				sensorCounter++;

				if (sensorCounter >= 5) {
					Sound.beep(); // just to test for lines
					makeCorrection();
				}
			}

			else {
				sensorCounter = 0; // reset in case of no black line
			}

			// ------------------------------------------------------------

			// ------------------------------------------------------------

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}

	public void makeCorrection() {
		boolean range1 = odometer.getTheta() > 88 && odometer.getTheta() < 92;
		boolean range2 = odometer.getTheta() > -92 && odometer.getTheta() < -88;
		boolean range3 = odometer.getTheta() > -182 && odometer.getTheta() < -178;
		boolean range4 = odometer.getTheta() > -272 && odometer.getTheta() < -268;

		if (range1) {

			if (!(odometer.getY() < 30.48)) {
				odometer.setY(30.48 + lastYValue);
			}

			lastYValue = odometer.getY();
		}

		if (range2) {

			if (!(odometer.getX() < 30.48)) {
				odometer.setX(30.48 + lastXValue);
			}

			lastXValue = odometer.getX();

		}

		if (range3) {

			odometer.setY(-30.48 + lastYValue);
			lastYValue = odometer.getY();

		}

		if (range4) {

			odometer.setX(-30.48 + lastXValue);
			lastXValue = odometer.getX();

		}
	}

	public int lightSensorReading() {
		return sensor.getRawLightValue();
	}

}