package lab3;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class NavigationObstacle extends Thread {

	private static final SensorPort usPort = SensorPort.S4;
	
	double x, y, xTarget, yTarget;
	
	double obstacleTheta;
	
	int countDistances;
	private final int max255Count = 20;
	
	boolean isNearObstacle = false;

	Odometer odometer;
	
	UltrasonicSensor usSensor = new UltrasonicSensor(usPort);

	public NavigationObstacle(Odometer odometer) {
		this.odometer = odometer;
	}

	private static final long NAVIGATION_PERIOD = 25;
	static final double leftRadius = 2.1;
	static final double rightRadius = 2.1;
	static final double wheelBase = 15.5;
	static final double errorThreshold = 1;

	private static final int ROTATE_SPEED = 180;
	private static final int FORWARD_SPEED = 360;

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	// COORDINATES
	Coordinate coord0 = new Coordinate(0, 0);
	Coordinate coord1 = new Coordinate(0, 60);
	Coordinate coord2 = new Coordinate(60, 0);

	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			double currentTheta = odometer.getTheta();
			
			if (currentTheta > 180) {
				odometer.setTheta(currentTheta - 360);
			} else if (currentTheta < -180) {
				odometer.setTheta(currentTheta + 360);
			}
			
			x = odometer.getX();
			y = odometer.getY();

			// TODO do not setTarget as frequently
			if (coord2.getIsVisited()) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			} else if (coord1.getIsVisited()) {
				setTarget(coord2);
				coord2.setIsVisited(x, y);
			} else {
				setTarget(coord1);
				coord1.setIsVisited(x, y);
			}
			
			if(!isNearObstacle){
				travelTo(xTarget, yTarget);
			}
			
			else {
				moveAroundObstacle();
			}
			
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < NAVIGATION_PERIOD) {
				try {
					Thread.sleep(NAVIGATION_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}

		}

	}

	public void travelTo(double xTarget, double yTarget) {

		// Determine whether to turn or not
		double xDiff = xTarget - this.x;
		double yDiff = yTarget - this.y;
		double targetTheta = Math.toDegrees(Math.atan2(yDiff, xDiff));

		double deltaTheta = targetTheta - odometer.getTheta();

		// if the heading is off, we must correct
		if (Math.abs(deltaTheta) > errorThreshold) {
			turnTo(targetTheta);
		} else {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
		}
		
		// if there is an obstacle, set boolean to true
		if(usSensor.getDistance() < 20){
			isNearObstacle = true;
			obstacleTheta = odometer.getTheta();
		}
		
	}

	public void turnTo(double targetTheta) {
		double currentTheta = odometer.getTheta();
		double rotateAmount = targetTheta - currentTheta;

		// to keep the angle in the range of -180 to 180
	
		// If at a point, turn to in place
		if (coord0.isAtPoint(x, y) || coord1.isAtPoint(x, y)
				|| coord2.isAtPoint(x, y)){

			// turn a minimal angle
			if (rotateAmount > 180) {
				rotateAmount = rotateAmount - 360;
			} else if (rotateAmount < -180) {
				rotateAmount = rotateAmount + 360;
			}

			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.rotate(-convertAngle(leftRadius, wheelBase, rotateAmount), true);
			rightMotor.rotate(convertAngle(rightRadius, wheelBase, rotateAmount), false);
		}
		// turn while travelling
		else if (rotateAmount > 15) {
			leftMotor.setSpeed(FORWARD_SPEED - 280); //good at 280
			rightMotor.setSpeed(FORWARD_SPEED);

		} else if (rotateAmount < -15) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED - 280);
		}
		else if (rotateAmount > 0){
			leftMotor.setSpeed(FORWARD_SPEED - 140);
			rightMotor.setSpeed(FORWARD_SPEED);
		}
		else if (rotateAmount < 0) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED - 140);
		}
		
	}
	
	public void moveAroundObstacle(){
		
		int distance = usSensor.getDistance();
		
		if(distance<20 || odometer.getTheta()>(obstacleTheta-75)){
			rightMotor.setSpeed(10);
			//isNearObstacle = true;
		}
		
		// if the max distance holds for a certain number of pings, go straight
		else if (distance >= 255 && countDistances <= max255Count) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			countDistances++;
			return;
		}

		else if (distance < 255) {
			countDistances = 0; // reset the counter when sensor not seeing max distance
		}
		
		else{
			isNearObstacle = false;
		}
	}

	public boolean isNavigating() {
		// TODO
		return false;
	}

	public void setTarget(Coordinate coord) {
		xTarget = coord.getX();
		yTarget = coord.getY();
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
