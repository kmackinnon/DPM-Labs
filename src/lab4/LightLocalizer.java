package lab4;

import lejos.nxt.LightSensor;

public class LightLocalizer {
	
	public final static double ROTATION_SPEED = 30;
	public final static double FORWARD_SPEED = 100;
	
	private Odometer odo;
	private TwoWheeledRobot robot;
	private LightSensor ls;
	
	public LightLocalizer(Odometer odo, LightSensor ls) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.ls = ls;
		
		// turn on the light
		ls.setFloodlight(true);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		
		
	
		
		
	}

}
