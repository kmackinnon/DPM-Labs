package lab1;

import lejos.nxt.*;

public class PController implements UltrasonicController {

	private final int bandCenter, bandwidth;
	private final int motorStraight = 200;
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;

	private final int max255Count = 24;

	private int distance;
	private int error;
	private int countDistances;
	private int motorMaxSpeed = 400;
	private int motorMinSpeed = 35;

	private int convexMotorHigh = 400;
	private int convexMotorLow = 60;

	private int newMotorHigh;
	private int newMotorLow;

	public PController(int bandCenter, int bandwidth) {
		// Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
	}

	@Override
	public void processUSData(int distance) {

		this.distance = distance;
		error = distance - bandCenter;

		// if the max distance holds for a certain number of pings, go straight
		if (distance >= 255 && countDistances <= max255Count) {
			goStraight();
			countDistances++;
			return;
		}

		if (distance < 255) {
			countDistances = 0; // reset counter when sensor not reading max distance
		}

		if (Math.abs(distance - bandCenter) <= bandwidth) { // within bandwidth
			goStraight();
		}

		else if (distance >= 255 && countDistances > max255Count) {
			turnConvexCorner(); // turn around a convex corner
		}

		else if (error > 0) { // case when far from wall
			turn(rightMotor, leftMotor); // turn left
		}

		else { // case when too close to wall
			turn(leftMotor, rightMotor); // turn right
		}

	}

	public void turn(NXTRegulatedMotor motorToSpeedUp, NXTRegulatedMotor motorToSlowDown) {

		newMotorHigh = motorStraight + Math.abs(error) * 8; // adjust speed based on error
		newMotorLow = motorStraight - Math.abs(error) * 8;

		// never surpass maximum speed
		if (newMotorHigh > motorMaxSpeed) {
			motorToSpeedUp.setSpeed(motorMaxSpeed);
		}

		else {
			motorToSpeedUp.setSpeed(newMotorHigh);
		}

		// never go below minimum speed
		if (newMotorLow < motorMinSpeed) {
			motorToSlowDown.setSpeed(motorMinSpeed);
		}

		else {
			motorToSlowDown.setSpeed(newMotorLow);
		}
	}

	public void turnConvexCorner() {
		rightMotor.setSpeed(convexMotorHigh);
		leftMotor.setSpeed(convexMotorLow);
	}

	public void goStraight() {
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
