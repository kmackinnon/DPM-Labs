package lab5;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class Scan{

	private Odometer odometer;
	final static double DEG_ERR = 2.0, CM_ERR = 0.5;

	ColorSensor cs = new ColorSensor(SensorPort.S1);
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

	public Scan(Odometer odo, UltrasonicSensor us) {
		this.odometer = odo;
		this.us = us;

		usMotor.setSpeed(25);
	}

	public void run() {
		cs.setFloodlight(Color.RED);
		turnSensor(-90);
		turn(90);
		travelTo(60, 0);
		
		if(isNotThereYet(60,0)){
			return;
		}
		
		turn(-90);
		
		travelTo(60,180);
		if(isNotThereYet(60,180)){
			return;
		}
	}

	public void travelTo(double x, double y) {

		boolean isObject = false;

		while (isNotThereYet(x, y)) {

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);

			leftMotor.forward();
			rightMotor.forward();

			if (us.getDistance() < 30) {
				isObject = true;
				break;
			}

/*			if (cs.getNormalizedLightValue() > 290) {
				leftMotor.setSpeed(0);
				rightMotor.setSpeed(0);
				break;
			}*/
		}

		if (isObject) {
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

}
