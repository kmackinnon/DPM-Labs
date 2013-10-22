package lab5;

import java.util.Arrays;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class BlockDetector extends Thread{

	public final Object lock = new Object(); // for blocking method

	ColorSensor cs = new ColorSensor(SensorPort.S1);
	Color color;
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	UltrasonicSensor us;

	private static final int TIME_PERIOD = 20;
	private static final int FORWARD_SPEED = 150;
	private static final int STOP_DISTANCE = 7;
	private static final double RIGHT_RADIUS = 2.1;
	private static final double LEFT_RADIUS = 2.1;
	public static final double WIDTH = 15.6;
	
	private boolean isStyro = false;
	private boolean isCinder = false;

	private int redValue, blueValue;
	
	private int distance, medianDistance;
	
	int[] distanceArray = new int[5];
	int[] sortedArray = new int[5];

	public BlockDetector(UltrasonicSensor us) {
		this.us = us;
	}

	public void run() {
		
		long timeStart, timeEnd;
		

		while (true) {
			timeStart = System.currentTimeMillis();
			
			//setMedian();
			
			if (us.getDistance() <= STOP_DISTANCE) {
				stop();
				
				setBlockType();

				if(isStyro){
					grabBlock();
				}
				
				else{
					stop();
				}
				
			} else {
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

	public int getBlue() {
		return blueValue;
	}

	public int getRed() {
		return redValue;
	}
	
	public void setBlockType(){
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

	public boolean getIsStyro() {
		return isStyro;
	}

	public boolean getIsCinder() {
		return isCinder;
	}
	
	
/*private void setMedian(){
		
		distance = us.getDistance();

		// shift each value to the left
		for (int i = 0; i < distanceArray.length - 1; i++) {
			distanceArray[i] = distanceArray[i + 1];
		}

		distanceArray[distanceArray.length - 1] = distance;

		System.arraycopy(distanceArray, 0, sortedArray, 0,
				distanceArray.length);
		Arrays.sort(sortedArray);
			
		medianDistance = median(sortedArray);	

	}
	
public int getMedian(){
	
	synchronized(lock){
	
		return medianDistance;
	
	}
}


private int median(int[] m) {
	
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2;
		}
}*/

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


	public void grabBlock(){
		goSetDistance(-10); // get the robot to move backwards so that it can
		// then either move around a wooden block or adjust
		// its position to push a styrofoam block
		
		turn(-90);
		
		goSetDistance(10);
		
		turn(90);
		
		goSetDistance(10);
	}
	
	
	public void goSetDistance(double distance) {
		leftMotor.setSpeed(-FORWARD_SPEED);
		rightMotor.setSpeed(-FORWARD_SPEED);
		rightMotor.rotate(convertDistance(RIGHT_RADIUS, distance), true);
		leftMotor.rotate(convertDistance(LEFT_RADIUS, distance), false);
	}
	
	public void turn(double angle){
		
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
}
