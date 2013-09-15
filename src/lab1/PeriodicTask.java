package lab1;
import java.util.TimerTask;

import lejos.nxt.UltrasonicSensor;


public class PeriodicTask extends TimerTask{

	private UltrasonicSensor us;
	private UltrasonicController cont;
	
	public PeriodicTask(UltrasonicSensor us, UltrasonicController cont){
		
		this.us = us;
		this.cont = cont;
		
	}
	
	public void run(){
		cont.processUSData(us.getDistance());
	}
	
}
