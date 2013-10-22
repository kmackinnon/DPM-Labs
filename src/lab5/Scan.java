package lab5;

/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import java.util.Arrays;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class Scan extends Thread {

	ColorSensor cs;
	Color color;
	UltrasonicSensor us;
	BlockDetector bd;
	Odometer odometer;

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	NXTRegulatedMotor usMotor = Motor.C;

	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 150;
	private static final double RIGHT_RADIUS = 2.1;
	private static final double LEFT_RADIUS = 2.1;
	private static final double WIDTH = 15.6;

	private static final int TIME_PERIOD = 20;
	private static final double CM_ERR = 1.5; // distance from correct position

	private int distance, medianDistance; // distance from a block
	private boolean isDone; // current routine completed

	int[] distanceArray = new int[5]; // distance readings
	int[] sortedArray = new int[5]; // distance readings in ascending order

	public Scan(Odometer odo, UltrasonicSensor us) {
		this.odometer = odo;
		this.us = us;
		this.cs = new ColorSensor(SensorPort.S1);

		usMotor.setSpeed(40);
	}

	// the idea of this method is to scan the perimeter with the sensor pointing
	// inwards
	public void doStuffRun() {
		isDone = false;

		turn(90); // turn robot clockwise
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.forward();
		rightMotor.forward();

		usMotor.setSpeed(80); // rotate sensor fast to be useful while moving
		turnSensor(-90); // rotate sensor counterclockwise

		// assuming we're near our starting position, travel to 60,0
		if (Math.abs(odometer.getX() - 60) > CM_ERR
				&& Math.abs(odometer.getY() - 0) < CM_ERR) {
			travelTo(60, 0);

			if (isNotThereYet(60, 0)) {
				isDone = true;
				return;
			}

			turn(-90); // turn CCW
		}

		// assuming we've reached our first waypoint, travel to 60,180
		else if (Math.abs(odometer.getX() - 60) < CM_ERR
				&& Math.abs(odometer.getY() - 180) > CM_ERR) {
			travelTo(60, 180);

			if (isNotThereYet(60, 180)) {
				isDone = true;
				return;
			}

			turn(-90); // turn CCW
		}

		else {
			isDone = true;
		}
	}

	// assuming the heading is correct, this method will get the robot to a
	// specific point
	public void travelTo(double x, double y) {
		boolean isObject = false;

		long timeStart, timeEnd;

		Arrays.fill(distanceArray, 255); // initializes the distanceArray

		while (isNotThereYet(x, y)) {
			timeStart = System.currentTimeMillis();

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();

			setMedian();

			// in the case that the median distance returned by the us is less
			// than 30, set isObject true
			if (medianDistance < 30) {
				isObject = true;
				break;
			}

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

		//once an object is found
		if (isObject) {
			usMotor.setSpeed(25);
			turnSensor(90);
			turn(-90);
		}

	}

	// determines if robot is near target
	public boolean isNotThereYet(double x, double y) {
		return Math.abs(x - odometer.getX()) > CM_ERR
				|| Math.abs(y - odometer.getY()) > CM_ERR;
	}

	// turns the robot by a specified angle
	public void turnSensor(int degrees) {
		usMotor.rotate(degrees);
	}

	// turns the robot by a specified angle
	public void turn(double angle) {
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(LEFT_RADIUS, WIDTH, angle), true);
		rightMotor.rotate(-convertAngle(RIGHT_RADIUS, WIDTH, angle), false);
	}

	// helper method to convert the distance each wheel must travel
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// helper method to convert the angle each motor must rotate
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	// calculates the median of a sorted array
	private int median(int[] m) {
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2;
		}
	}

	// sets the median with a moving window of last 5 us readings
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

	public boolean getIsDone() {
		return isDone;
	}

}
