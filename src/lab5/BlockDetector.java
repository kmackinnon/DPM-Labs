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

public class BlockDetector extends Thread {

	public final Object lock = new Object(); // for blocking method

	ColorSensor cs;
	Color color;
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	UltrasonicSensor us;
	Odometer odo;

	private static final int TIME_PERIOD = 20;
	private static final int FORWARD_SPEED = 150;
	private static final int STOP_DISTANCE = 7; // how far from block to stop
	private static final double RIGHT_RADIUS = 2.1;
	private static final double LEFT_RADIUS = 2.1;
	private static final double WIDTH = 15.6;
	private static final double CM_ERR = 1.5; // allowed error in position

	private boolean isStyro = false; // initialize to false
	private boolean isCinder = false;
	private boolean doneForNow;

	private int redValue, blueValue; // color readings

	private int distance, medianDistance;

	int[] distanceArray = new int[5];
	int[] sortedArray = new int[5];

	public BlockDetector(Odometer odo, UltrasonicSensor us) {
		this.odo = odo;
		this.us = us;
		this.cs = new ColorSensor(SensorPort.S1);
	}

	public void doStuffRun() {
		doneForNow = false;

		long timeStart, timeEnd;

		double xInit = odo.getX(); // initial x position
		double yInit = odo.getY(); // initial y position
		Arrays.fill(distanceArray, 255);

		while (true) {
			timeStart = System.currentTimeMillis();

			setMedian();

			// stop if close to a block
			if (medianDistance <= STOP_DISTANCE) {
				stop();
				setBlockType(); // determine block type

				if (isStyro) {
					grabBlock();
				} else {
					break;
				}

			} else {
				goForward();
				isCinder = false;
				isStyro = false;
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

		while (isNotThereYet(xInit, yInit)) {
			goBackward();
		}

		stop();
		doneForNow = true;
		return;

	}

	// accessors
	public int getBlue() {
		return blueValue;
	}

	public int getRed() {
		return redValue;
	}

	public boolean getIsStyro() {
		return isStyro;
	}

	public boolean getIsCinder() {
		return isCinder;
	}

	// determines the type of a block through the use of color ratios
	public void setBlockType() {
		color = cs.getColor();
		redValue = color.getRed();
		blueValue = color.getBlue();

		double ratio = (double) redValue / (double) blueValue;

		if (ratio > 1.8) {
			isStyro = false;
			isCinder = true;
		} else {
			isCinder = false;
			isStyro = true;
		}
	}

	public int getMedian() {
		synchronized (lock) {
			return medianDistance;
		}
	}

	// moving window to set median distance
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

	// calculates median of a sorted array
	private int median(int[] m) {
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2;
		}
	}

	// travel in a straight line indefinitely
	public void goForward() {
		leftMotor.forward();
		rightMotor.forward();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
	}

	// travel backwards indefinitely
	public void goBackward() {
		leftMotor.backward();
		rightMotor.backward();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
	}

	// stops the robot
	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}

	// method adjusts robot's position to capture the styrofoam block
	public void grabBlock() {
		goSetDistance(-10); // get the robot to move backwards so that it can
		// then either move around a wooden block or adjust
		// its position to push a styrofoam block

		turn(-90); // CCW
		goSetDistance(10);
		turn(90); // CW
		goSetDistance(10);

		// goes forward indefinitely as we didn't implement a method of
		// travelling to corner properly
		while (true) {
			goForward();
		}
	}

	// travels a specific distance
	public void goSetDistance(double distance) {
		leftMotor.setSpeed(-FORWARD_SPEED);
		rightMotor.setSpeed(-FORWARD_SPEED);
		rightMotor.rotate(convertDistance(RIGHT_RADIUS, distance), true);
		leftMotor.rotate(convertDistance(LEFT_RADIUS, distance), false);
	}

	// turns the robot
	public void turn(double angle) {
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

	// returns a boolean telling whether the robot is at target location
	public boolean isNotThereYet(double x, double y) {
		return Math.abs(x - odo.getX()) > CM_ERR
				|| Math.abs(y - odo.getY()) > CM_ERR;
	}

	public boolean isDoneForNow() {
		return doneForNow;
	}

}
