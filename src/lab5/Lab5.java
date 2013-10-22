package lab5;

/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class Lab5 {

	final static long TIME_PERIOD = 50;

	public static void main(String[] args) {
		int buttonChoice;

		TwoWheeledRobot patBot = new TwoWheeledRobot(Motor.A, Motor.B);
		Odometer odo = new Odometer(patBot, true);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);

		USLocalizer usLoc = new USLocalizer(odo, us,
				USLocalizer.LocalizationType.FALLING_EDGE);

		do {
			// clear the display
			LCD.clear();

			// ask the user whether to navigate with or without the obstacle
			LCD.drawString("< Left  |Right >", 0, 0);
			LCD.drawString("        |       ", 0, 1);
			LCD.drawString("Localize|Detect ", 0, 2);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		// for testing, we added this to avoid localizing each time
		if (buttonChoice == Button.ID_LEFT) {
			usLoc.doLocalization();
			Sound.beep(); // just to indicate beginning of block detection
		}

		Scan scan = new Scan(odo, us);
		BlockDetector bd = new BlockDetector(odo, us);
		new LCDInfo(bd, scan, us);

		// begin the threads
		scan.start();
		bd.start();

		while (true) {
			scan.doStuffRun();

			// if a scanning routine needs to perform block detection
			if (scan.getIsDone()) {
				bd.doStuffRun();
			}
		}
	}
}
