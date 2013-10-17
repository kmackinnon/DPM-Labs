package lab5;

import lab4.TwoWheeledRobot;
import lejos.nxt.Button;
import lejos.nxt.Motor;

public class Lab5 {

	final static long TIME_PERIOD = 50;

	static TwoWheeledRobot robot = new TwoWheeledRobot(Motor.A, Motor.B);

	public static void main(String[] args) {

		int buttonChoice;
		BlockDetector bd = new BlockDetector();

		new LCDInfo(bd);
		
		buttonChoice = Button.waitForAnyPress();
		bd.run();

		while (buttonChoice != Button.ID_ESCAPE);
		System.exit(0);

	}
}
