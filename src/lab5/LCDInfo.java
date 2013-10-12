package lab5;
/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Timer lcdTimer;
	BlockDetector bd = new BlockDetector();
	
	public LCDInfo(BlockDetector bd) {
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
		this.bd = bd;
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() { 
		LCD.clear();
		
		LCD.drawString("Color: ", 0, 0);
		LCD.drawString("Dista: ", 0, 1);
		
		LCD.drawInt(bd.getColor(), 8, 0);
		LCD.drawInt(bd.getDistance(), 8, 1);
		
		LCD.drawString(bd.blockType(), 0, 2);
		
	}
}
