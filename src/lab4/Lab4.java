package lab4;
/*
 * Keith MacKinnon (260460985)
 * Takeshi Musgrave (260527485)
 * Fall 2013, DPM, Group 26
 */

import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class Lab4 {

	public static void main(String[] args) {
		// setup the odometer, display, and ultrasonic and light sensors
		TwoWheeledRobot patBot = new TwoWheeledRobot(Motor.A, Motor.B);
		Odometer odo = new Odometer(patBot, true);
		
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		ColorSensor cs = new ColorSensor(SensorPort.S1);
	
		int buttonChoice;
		
		do {
			// clear the display
			LCD.clear();

			// ask the user to do either rising or falling edge
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString("RISING | FALLING", 0, 2);
			
			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);
  
		USLocalizer usl;
		
		// button choice determines ultrasonic localization method
		if (buttonChoice == Button.ID_LEFT) {
			usl = new USLocalizer(odo, us, USLocalizer.LocalizationType.RISING_EDGE);
		} else { // right button	
			usl = new USLocalizer(odo, us, USLocalizer.LocalizationType.FALLING_EDGE);
		}
				
		LCDInfo lcd = new LCDInfo(odo, usl);
		
		usl.doLocalization();
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odo, cs);
		lsl.doLocalization();			
		
		while (buttonChoice != Button.ID_ESCAPE);
		System.exit(0);
	}

}
