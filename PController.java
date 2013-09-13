import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;
	private final int multiplier = 2;
	
	
	private int distance;
	private int currentLeftSpeed;
	private int currentRightSpeed;
	private int newLeftSpeed;
	private int newRightSpeed;
	private int filterControl;
	
	private int difference;
	
	private int speedIncrement;
	
	public PController(int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		currentLeftSpeed = 0;
		currentRightSpeed = 0;
		filterControl = 0;
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
		// TODO: process a movement based on the us distance passed in (P style)
		
		
		/*difference = distance - bandCenter;
		currentLeftSpeed = leftMotor.getSpeed();
		currentRightSpeed = rightMotor.getSpeed();
		
		if(Math.abs(difference)<=bandwidth){
			return;
		}
		
		else{
			speedIncrement = -1*difference*multiplier;
			newLeftSpeed = currentLeftSpeed + speedIncrement;
			leftMotor.setSpeed(newLeftSpeed);
		}*/
		
		
	}

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
