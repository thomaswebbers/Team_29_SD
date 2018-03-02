package main.java.softdesign;

public class Supply {
	
	double size;
	String supplyName;
	
	public Supply (String supplyName, double size){
		this.size = size;
		this.supplyName = supplyName;
	}
	
	public double getSize(){
		return size;
	}
	
	public String getName(){
		return supplyName;
	}

}
