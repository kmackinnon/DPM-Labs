package lab3;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Lab3 {
	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		Odometer odometer = new Odometer();
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer);
		Navigation navigation = new Navigation(odometer);
		NavigationObstacle navigationObstacle = new NavigationObstacle(odometer);

		do {
			// clear the display
			LCD.clear();

			// ask the user whether to navigate with or without the obstacle
			LCD.drawString("< Left   | Right >", 0, 0);
			LCD.drawString("         |        ", 0, 1);
			LCD.drawString("Navigate | Obstacle", 0, 2);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			for (NXTRegulatedMotor motor : new NXTRegulatedMotor[] { Motor.A,
					Motor.B, Motor.C }) {
				motor.forward();
			}

			// start navigation without obstacle
			odometer.start();
			odometryDisplay.start();
			navigation.start();

		} else {
			// start navigation with obstacle
			odometer.start();
			odometryDisplay.start();
			navigationObstacle.start();
		}

		// to end the program
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
