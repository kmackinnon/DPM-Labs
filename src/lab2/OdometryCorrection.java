package lab2;

// Keith MacKinnon (260460985)
// Takeshi Musgrave (260527485)

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;

/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final int MIN_READINGS = 5; // acts as a filter to ensure lines	
	private static final int THRESHOLD = 520; // if lower than this value, black line
	private static final double OFFSET = 5.5; // difference between sensor and wheels
	private static final double HALF_SQUARE = 15.24; // a square is 30.48cm
	
	private Odometer odometer;
	private ColorSensor sensor = new ColorSensor(SensorPort.S1); //light sensor

	private int sensorCounter = 0; //used to ensure line crossing

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		sensor.setFloodlight(true); // turns on the red light of the sensor
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// put your correction code here
			if (lightSensorReading() < THRESHOLD) { // to detect black lines
				sensorCounter++;

				// if the robot is crossing a line, update x and y coordinates respectively
				if (sensorCounter >= MIN_READINGS) {
					Sound.beep(); // to aid in debugging -- to test for lines
					makeCorrection();
				}
			}

			else {
				sensorCounter = 0; // reset in case of no black line
			}

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
		
		// these booleans are to check which leg of the track the robot is in
		boolean range1 = odometer.getTheta() > 87 && odometer.getTheta() < 93;
		boolean range2 = odometer.getTheta() > -3 && odometer.getTheta() < 3;
		boolean range3 = odometer.getTheta() > -93 && odometer.getTheta() < -87;
		boolean range4 = odometer.getTheta() > -183 && odometer.getTheta() < -177;
		
		int multiplier = 1; // for range 1 and 2, multiply offset so negative

		if (range3 || range4){ // for range 3 and 4, multiply offset so positive
			multiplier = -1;
		}

		// need to adjust y value to nearest 15.24 upon crossing a line
		if (range1 || range3) {
			odometer.setY((Math.round(odometer.getY() / HALF_SQUARE) * HALF_SQUARE) -OFFSET*multiplier);
		}
		
		// need to adjust x value to nearest 15.24 upon crossing a line
		if (range2 || range4) {
			odometer.setX((Math.round(odometer.getX() / HALF_SQUARE) * HALF_SQUARE) -OFFSET*multiplier);
		}
	}

	// returns the light value of the sensor
	public int lightSensorReading() {
		return sensor.getRawLightValue();
	}

}