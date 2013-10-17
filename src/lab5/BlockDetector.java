package lab5;

import java.util.Arrays;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class BlockDetector extends Thread {

	ColorSensor cs = new ColorSensor(SensorPort.S1);
	UltrasonicSensor us;
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;

	final int BLUE_STYRO = 290;
	final int CINDER_BLOCK = 210;
	final int COLOR_TOLERANCE = 40;
	final int TIME_PERIOD = 20;
	final int FORWARD_SPEED = 150;
	final int STOP_DISTANCE = 7;

	private boolean isStyro = false;
	private boolean isCinder = false;
	
	int redValue, blueValue;
	
	Color color;

	private int distance, medianDistance;
	
	public BlockDetector(UltrasonicSensor us){
		this.us = us;
	}	

	public void run() {

        int[] distanceArray = new int[5];
        
        int[] sortedArray = new int[5];
        Arrays.fill(distanceArray, 255);
       
		long timeStart, timeEnd;

		leftMotor.forward();
		rightMotor.forward();

		while (true) {

			timeStart = System.currentTimeMillis();
			distance = us.getDistance();
			
			for(int i = 0; i<distanceArray.length-1; i++){
				distanceArray[i] = distanceArray[i+1];
			}
			
			distanceArray[distanceArray.length-1] = distance;
			
	        System.arraycopy(distanceArray, 0, sortedArray, 0, distanceArray.length);
	        Arrays.sort(sortedArray);
	        
	        medianDistance = median(sortedArray);

			if (medianDistance <= STOP_DISTANCE) {
				stop();
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

	public boolean getIsStyro(){
		return isStyro;
	}
	
	public boolean getIsCinder(){
		return isCinder;
	}
	
	public int getDistance() {
		return distance;
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
	
    public static int median(int[] m) {
        int middle = m.length/2;
        if (m.length%2 == 1) {
            return m[middle];
        } else {
            return (m[middle-1] + m[middle]) / 2;
        }
    }
	

}
