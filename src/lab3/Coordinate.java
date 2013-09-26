package lab3;

public class Coordinate {

	private double x;
	private double y;
	private boolean isVisited = false;
	
	public Coordinate (double x, double y){
		this.x = x;
		this.y = y;
	}
	
	// getters
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public boolean getIsVisited(){
		return isVisited;
	}
	
	// sets isVisited true or false 
	public void setIsVisited(double x, double y){
		isVisited = x==getX() && y==getY();		
	}
}
