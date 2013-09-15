import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private final int motorStraight = 200;
	
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;
	
	private final int max255Count = 25;
	
	private int distance;
	private int error;
	private int countDistances;
	
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
		
	}
	
	@Override
	public void processUSData(int distanceArgument) {
		
		distance = distanceArgument;
		error = distance - bandCenter;
		
		if(distance >= 255 && countDistances <= max255Count){
			goStraight();
			countDistances++;
			return;
		}
		
		if(distance<255){
			countDistances=0; //reset
		}
		
		if(Math.abs(distance-bandCenter) <= bandwidth){
			
			goStraight();
			
		}
		
		else if(distance >= 255 && countDistances > max255Count ){
			turnConvexCorner();
		}
		
		else if (error > 0){ // case when far from wall
			turn(rightMotor, leftMotor); //turn left
		}
		
		else { // case when too close to wall
			turn(leftMotor,rightMotor); //turn right
		}
		
	}	
		
	public void turn(NXTRegulatedMotor motorToSpeedUp, NXTRegulatedMotor motorToSlowDown){
			motorToSpeedUp.setSpeed(motorHigh);
			motorToSlowDown.setSpeed(motorLow);
	}
	
	public void turnConvexCorner(){
		rightMotor.setSpeed(motorHigh);
		leftMotor.setSpeed(motorLow);
	}
	
	public void goStraight(){
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
