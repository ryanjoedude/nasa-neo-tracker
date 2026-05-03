/**
 * neoObject Class
 * @author Ryan J. Brady
 * 
 * This class represents a Near-Earth Object retrieved from
 * the NASA NEO API. It stores data fields: name, missDistance
 * velocity, minDiam (minimum estimated diameter), maxDiam
 * (maximum estimated diameter), date, and hazardous (boolean)
 * 
 */



public class neoObject {
	String name;
	double missDistance;
	double velocity;
	double minDiam;
	double maxDiam;
	String date;
	boolean hazardous;
	
	// constructor for neoObject
	public neoObject(String name, double missDistance, double velocity, double minDiam, double maxDiam, String date, boolean hazardous) {
		this.name = name;
		this.missDistance = missDistance;
		this.velocity = velocity;
		this.minDiam = minDiam;
		this.maxDiam = maxDiam;
		this.date = date;
		this.hazardous = hazardous;
	}
	
	/**
	 * Getter for miss distance of object
	 * @return missDistance in miles
	 */
	public double getMissDistance() {
		return missDistance;
	}
	
}