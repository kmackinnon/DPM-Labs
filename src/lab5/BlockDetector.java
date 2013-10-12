package lab5;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class BlockDetector extends Thread {

	ColorSensor cs = new ColorSensor(SensorPort.S1);
	UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
	// TwoWheeledRobot robot = new TwoWheeledRobot(Motor.A, Motor.B);
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	final int BLUE_STYRO = 290;
	final int CINDER_BLOCK = 210;
	final int COLOR_TOLERANCE = 40;
	final int TIME_PERIOD = 20;
	final int FORWARD_SPEED = 150;
	final int STOP_DISTANCE = 7;

	boolean isStyro = false;
	boolean isCinder = false;

	private int distance, colorValue;

	public void run() {

		cs.setFloodlight(Color.BLUE);
		long timeStart, timeEnd;

		leftMotor.forward();
		rightMotor.forward();

		while (true) {

			timeStart = System.currentTimeMillis();

			distance = us.getDistance();
			colorValue = cs.getRawLightValue();

			if (distance <= STOP_DISTANCE) {
				stop();

				if (Math.abs(colorValue - BLUE_STYRO) < COLOR_TOLERANCE) {
					isStyro = true;
					isCinder = false;
				}

				else if (Math.abs(colorValue - CINDER_BLOCK) < COLOR_TOLERANCE) {
					isCinder = true;
					isStyro = false;
				}

				else {
					isCinder = false;
					isStyro = false;
				}

			}

			else {
				goStraight();

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
	}

	public int getColor() {
		return colorValue;
	}

	public int getDistance() {
		return distance;
	}

	public String blockType() {

		if (isStyro) {
			return "Styrofoam";
		}

		else if (isCinder) {
			return "Cinder";
		}

		else {
			return "Neither";
		}

	}

	public void goStraight() {
		leftMotor.forward();
		rightMotor.forward();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
	}

	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}

}
