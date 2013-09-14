import lejos.nxt.*;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200;
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;
	
	
	private final int max255Count = 20;
	
	private int distance;
	private int error;
	private int countDistances;

	private int motorHigh = 300;
	private int motorLow = 100;
	private int motorMaxSpeed = 400;
	private int motorMinSpeed = 35;
	
	private int convexMotorHigh = 350;
	private int convexMotorLow = 30;
	
	private int newMotorHigh;
	private int newMotorLow;
	
	public PController(int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
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
		
		newMotorHigh = motorStraight + Math.abs(error)*7;
		newMotorLow = motorStraight - Math.abs(error)*7;
		
		if(newMotorHigh>motorMaxSpeed){
			motorToSpeedUp.setSpeed(motorMaxSpeed);
		}
		
		else{
			motorToSpeedUp.setSpeed(newMotorHigh);
		}
		
		if(newMotorLow<motorMinSpeed){
			motorToSlowDown.setSpeed(motorMinSpeed);
		}
		
		else{
			motorToSlowDown.setSpeed(newMotorLow);
		}
	}
	
	public void turnConvexCorner(){
		rightMotor.setSpeed(convexMotorHigh);
		leftMotor.setSpeed(convexMotorLow);
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
