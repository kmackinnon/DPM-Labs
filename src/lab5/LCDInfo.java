package lab5;

/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import lejos.nxt.LCD;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDInfo implements TimerListener {
	public static final int LCD_REFRESH = 100;
	private Timer lcdTimer;
	BlockDetector bd;
	UltrasonicSensor us;
	Scan scan;

	public LCDInfo(BlockDetector bd, Scan scan, UltrasonicSensor us) {
		this.lcdTimer = new Timer(LCD_REFRESH, this);

		this.us = us;
		this.bd = bd;
		this.scan = scan;

		// start the timer
		lcdTimer.start();
	}

	public void timedOut() {
		LCD.clear();

		LCD.drawString("Blue: ", 0, 0);
		LCD.drawString("Red: ", 0, 1);
		LCD.drawString("Dist: ", 0, 2);

		LCD.drawInt(bd.getBlue(), 6, 0);
		LCD.drawInt(bd.getRed(), 6, 1);
		LCD.drawInt(us.getDistance(), 6, 2);

		if (bd.getIsCinder() || bd.getIsStyro()) {
			LCD.drawString("Object Detected", 0, 3);
		}

		if (bd.getIsStyro()) {
			LCD.drawString("Block", 0, 4);
		} else if (bd.getIsCinder()) {
			LCD.drawString("Not Block", 0, 4);
		}
		
		
		LCD.drawString("X: ", 0, 5);
		LCD.drawString("Y: ", 0, 6);
		LCD.drawInt((int)scan.odometer.getX(), 4, 5);
		LCD.drawInt((int)scan.odometer.getY(), 4, 6);

	}
}
