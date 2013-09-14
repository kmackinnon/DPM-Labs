import lejos.nxt.UltrasonicSensor;
import java.util.Timer;


public class UltrasonicPoller extends Thread{
	private UltrasonicSensor us;
	private UltrasonicController cont;
	
	public UltrasonicPoller(UltrasonicSensor us, UltrasonicController cont) {
		this.us = us;
		this.cont = cont;
	}
	
	public void run() {
		
		Timer timer = new Timer();

		timer.schedule(new PeriodicTask(us,cont),0,100);
		
	}

}
