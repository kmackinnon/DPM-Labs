import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private final int motorStraight = 200, FILTER_OUT = 20;
	
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;
	
	private int distance;
	private int filterControl;
	private int changeInDistance;
	private int rotationTime;
	private int rotationTimeLimit;
	private boolean programJustStarted;
	private boolean resetRotationTime;

	
	private int[] distanceArray = new int[10];
	
	
	public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		rotationTime=0;
		programJustStarted = true;
		resetRotationTime= false;
		
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
		}
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
	
		
		if(distance==255){
			
			turnConvexCorner();
			
		}
		
		else{
			
			if(programJustStarted){
				for(int i=0;i<9;i++){
					distanceArray[i]=distance;
				}
				programJustStarted=false;
			}
			
			else{
				for(int i=0;i<9;i++){
					distanceArray[i]=distanceArray[i+1];
				}
			}
			
			distanceArray[9] = distance;

			changeInDistance = distanceArray[9]-distanceArray[0]; //about 1 second ago
			
			if(Math.abs(distance - bandCenter)<=bandwidth){ //case when we are within the bandwidth
				
				if(changeInDistance>1){ // moving away from wall
					turnForABit(rightMotor,leftMotor);
				}
				
				else if(changeInDistance<-1){ // moving towards wall
					turnForABit(leftMotor,rightMotor);
				}
				
				else{
					// just keep going straight
					goStraight();
				}
			}
				
			
			else{ // further than 10cm on each side from band center
			
				if(distance>bandCenter){ // case when far from wall
	
					turnLeft();
						
				}
				
				else if(distance<bandCenter){ // case when too close to wall

					turnRight();
		
				}
			}
		}
		
		
	}

	
	
	
	public void turnRight(){
		rotationTimeLimit = 20;
		
		if(rotationTime<rotationTimeLimit){
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorLow);
			rotationTime++;
		}
		
		else{
			goStraight();
		}
		

	}
	
	public void turnLeft(){
		
		rotationTimeLimit = 20;
		
		if(rotationTime<rotationTimeLimit){
			rightMotor.setSpeed(motorHigh);
			leftMotor.setSpeed(motorLow);
			rotationTime++;
		}
		
		else{
			goStraight();
		}
		
		
	}
	
	public void goStraight(){
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
	}
	
	public void turnConcaveCorner(){
		
	}
	
	public void turnConvexCorner(){
		
	}
	
	
	public void turnForABit(NXTRegulatedMotor motorToSpeedUp, NXTRegulatedMotor motorToSlowDown){
		
		if(rotationTime==0){
			rotationTimeLimit = Math.abs(changeInDistance*5);
		}
		
		if(rotationTime<=rotationTimeLimit){
			motorToSpeedUp.setSpeed(motorHigh);
			motorToSlowDown.setSpeed(motorLow);
			rotationTime++;
		}
		
		if(rotationTime>rotationTimeLimit){
			rotationTime=0;
		}
		
	}
	
	
	
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
	

}


















