package lab4;

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private USLocalizer uslocalizer;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo, USLocalizer uslocalizer) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.uslocalizer = uslocalizer;
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() { 
		odo.getPosition(pos);
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("T: ", 0, 2);
		LCD.drawString("D: ", 0, 3);
		LCD.drawString("A: ", 0, 4);
		LCD.drawString("B: ", 0, 5);
		LCD.drawInt((int)(pos[0] * 10), 3, 0);
		LCD.drawInt((int)(pos[1] * 10), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		LCD.drawInt((int)uslocalizer.getCurrentDistance(), 3, 3);
		LCD.drawInt((int)uslocalizer.getAngleA(), 3, 4);
		LCD.drawInt((int)uslocalizer.getAngleB(), 3, 5);
	}
}
