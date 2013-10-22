package lab5;

import java.util.Arrays;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class Scan extends Thread {

	Odometer odometer;
	final static double DEG_ERR = 2.0, CM_ERR = 1.5;

	ColorSensor cs;
	Color color;
	UltrasonicSensor us;
	BlockDetector bd;

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	NXTRegulatedMotor usMotor = Motor.C;

	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 150;
	private static final double RIGHT_RADIUS = 2.1;
	private static final double LEFT_RADIUS = 2.1;
	private static final double WIDTH = 15.6;
	private static final int TIME_PERIOD = 20;

	private int distance, medianDistance;
	private boolean doneForNow;

	int[] distanceArray = new int[5];
	int[] sortedArray = new int[5];

	public Scan(Odometer odo, UltrasonicSensor us) {
		this.odometer = odo;
		this.us = us;
		this.cs = new ColorSensor(SensorPort.S1);

		usMotor.setSpeed(20);
	}

	public void run() {

	}

	public void doStuffRun() {
		doneForNow = false;
		// cs.setFloodlight(Color.RED);
		turn(90);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.forward();
		rightMotor.forward();
		usMotor.setSpeed(80);
		turnSensor(-90);

		if (Math.abs(odometer.getX() - 60) > CM_ERR
				&& Math.abs(odometer.getY() - 0) < CM_ERR) {
			travelTo(60, 0);

			if (isNotThereYet(60, 0)) {
				doneForNow = true;
				return;
			}

			turn(-90);
		}

		if (Math.abs(odometer.getX() - 60) < CM_ERR
				&& Math.abs(odometer.getY() - 180) > CM_ERR) {
			travelTo(60, 180);

			if (isNotThereYet(60, 180)) {
				doneForNow = true;
				return;
			}

			turn(-90);
		}

		else {
			doneForNow = true;
		}
	}

	public void travelTo(double x, double y) {

		boolean isObject = false;

		long timeStart, timeEnd;

		Arrays.fill(distanceArray, 255);

		while (isNotThereYet(x, y)) {

			timeStart = System.currentTimeMillis();

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);

			leftMotor.forward();
			rightMotor.forward();

			setMedian();

			if (medianDistance < 30) {
				isObject = true;
				break;
			}

			/*
			 * if (cs.getNormalizedLightValue() > 290) { leftMotor.setSpeed(0);
			 * rightMotor.setSpeed(0); break; }
			 */

			timeEnd = System.currentTimeMillis();
			if (timeEnd - timeStart < TIME_PERIOD) {
				try {
					Thread.sleep(TIME_PERIOD - (timeEnd - timeStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the detector will be
					// interrupted by another thread
				}
			}

		}

		if (isObject) {
			usMotor.setSpeed(20);
			turnSensor(90);
			turn(-90);
		}

	}

	public boolean isNotThereYet(double x, double y) {
		return Math.abs(x - odometer.getX()) > CM_ERR
				|| Math.abs(y - odometer.getY()) > CM_ERR;
	}

	public void turnSensor(int degrees) {
		usMotor.rotate(degrees);
	}

	public void turn(double angle) {
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(LEFT_RADIUS, WIDTH, angle), true);
		rightMotor.rotate(-convertAngle(RIGHT_RADIUS, WIDTH, angle), false);
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	private int median(int[] m) {
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2;
		}
	}

	private void setMedian() {
		distance = us.getDistance();

		// shift each value to the left
		for (int i = 0; i < distanceArray.length - 1; i++) {
			distanceArray[i] = distanceArray[i + 1];
		}

		distanceArray[distanceArray.length - 1] = distance;

		System.arraycopy(distanceArray, 0, sortedArray, 0, distanceArray.length);
		Arrays.sort(sortedArray);

		medianDistance = median(sortedArray);
	}

	public int getMedian() {
		return medianDistance;
	}

	public boolean isDoneForNow() {
		return doneForNow;
	}

}
