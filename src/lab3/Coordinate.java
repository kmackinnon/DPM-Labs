package lab3;

public class Coordinate {

	private double x;
	private double y;
	private boolean isVisited = false;
	private final double NEARBY = 2;

	public Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// getters
	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public boolean getIsVisited() {
		return isVisited;
	}

	// sets isVisited true or false
	public void setIsVisited(double x, double y) {
		isVisited = (Math.abs(x - getX()) < NEARBY)
				&& (Math.abs(y - getY()) < NEARBY);
	}
	
	// returns whether the robot is at a specific point
	public boolean isAtPoint(double x, double y){
		return (Math.abs(x - getX()) < NEARBY) && (Math.abs(y - getY()) < NEARBY);
		
	}
	
}