import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private final int highSpeed = 300, lowSpeed = 150;
	private final int sensorHighSpeed = 20;
	private final int sensorLowSpeed = 8;
	
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C, sensorMotor = Motor.B;
	
	private int distance;
	private int currentLeftSpeed;
	private int currentRightSpeed;
	private int newLeftSpeed;
	private int newRightSpeed;
	private int filterControl;
	private int changeInDistance;
	private int rotationCounter;
	private int rotationCounterLimit;

	
	private int[] distanceArray = new int[10];
	

	private int difference;
	
	public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		sensorMotor.setSpeed(0);
		leftMotor.forward();
		rightMotor.forward();
		currentLeftSpeed = 0;
		currentRightSpeed = 0;
		rotationCounter=0;
		
	}
	
	@Override
	public void processUSData(int distance) {
		// rudimentary filter
		if (distance == 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distance == 255){
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
			
			for(int i=0;i<9;i++){
				distanceArray[i]=distanceArray[i+1];
			}
			
			distanceArray[9] = distance;
			
		}
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
	
		changeInDistance = distanceArray[9]-distanceArray[0]; //about 1 second ago
		currentLeftSpeed = leftMotor.getSpeed();
		currentRightSpeed = rightMotor.getSpeed();
		
		if(Math.abs(distance - bandCenter)<=bandwidth){ //case when we are within the bandwidth
			
			// just keep going straight
			leftMotor.setSpeed(motorStraight);
			rightMotor.setSpeed(motorStraight);
			sensorMotor.stop();
		}
		
		else if(Math.abs(distance - bandCenter)<=10){ // case when within 10 cm of band center
		
			if(changeInDistance>1){ // moving away from wall
				if(rotationCounter==0){
					rotationCounterLimit = changeInDistance*5;
				}
				
				if(rotationCounter<=rotationCounterLimit){
					rightMotor.setSpeed(highSpeed);
					leftMotor.setSpeed(lowSpeed);
					sensorMotor.setSpeed(sensorHighSpeed);
					sensorMotor.forward();
					rotationCounter++;
				}
				
				if(rotationCounter>rotationCounterLimit){
					rotationCounter=0;
					sensorMotor.stop();
				}
			}
			
			else if(changeInDistance<-1){ // moving towards wall
				if(rotationCounter==0){
					rotationCounterLimit = Math.abs(changeInDistance*5);
				}
				
				if(rotationCounter<=rotationCounterLimit){
					leftMotor.setSpeed(highSpeed);
					sensorMotor.setSpeed(sensorHighSpeed);
					sensorMotor.backward();
					rotationCounter++;
				}
				
				if(rotationCounter>rotationCounterLimit){
					rotationCounter=0;
					sensorMotor.stop();
				}
			}
			
		}
		
		else{ // further than 10cm on each side from band center
		
			if(distance>bandCenter){ // case when far from wall
			
				rightMotor.setSpeed(motorStraight);
				leftMotor.setSpeed(lowSpeed);
				
				sensorMotor.setSpeed(sensorLowSpeed);
				sensorMotor.forward();
				// TODO: we have to stop the motor from going too far
			}
			
			else if(distance<bandCenter){ // case when too close to wall
				leftMotor.setSpeed(highSpeed);
				
				rightMotor.setSpeed(lowSpeed);
				
				sensorMotor.setSpeed(sensorHighSpeed);
				sensorMotor.backward();
				
			}
		}
		
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
