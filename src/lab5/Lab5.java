package lab5;

import lejos.nxt.Button;
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


		
		USLocalizer usl = new USLocalizer(odo, us, USLocalizer.LocalizationType.FALLING_EDGE);

		
		
		buttonChoice = Button.waitForAnyPress();

		usl.doLocalization();
		
		Sound.beep();
		
		UltrasonicSensor us2 = new UltrasonicSensor(SensorPort.S2);
		
		BlockDetector bd = new BlockDetector(us2);
		
		new LCDInfo(bd,usl);
		
		bd.run();

		while (buttonChoice != Button.ID_ESCAPE);
		System.exit(0);

	}
}
