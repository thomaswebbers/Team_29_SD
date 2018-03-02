package main.java.softdesign;

import java.util.ArrayList;

public class SupplyBox {
	private ArrayList<Supply> supplies;
	private double capacity;
	private double initialCapacity;
	
	public SupplyBox(double initialCapacity){
		supplies = new ArrayList<Supply>();
		capacity = initialCapacity;
		this.initialCapacity = initialCapacity;
	}
	
	public ArrayList<Supply> getAllSupplies(){
		ArrayList<Supply> result = new ArrayList<Supply>();
		result.addAll(supplies);
		supplies.clear();
		capacity = initialCapacity;
		return result;
	}
	
	public Supply getSupply(String supplyName){
		Supply supply = this.contains(supplyName);
		if(supply == null){
			return null;
		}
		double supplySize = supply.getSize();
		capacity += supplySize;
		supplies.remove(supply);
		return supply;
	}

	public boolean addSupply(String supplyName, double supplySize){
		if(supplySize > capacity){
			return false;
		}
		capacity -= supplySize;
		Supply supply = new Supply(supplyName, supplySize);
		supplies.add(supply);
		return true;
	}
	
	private Supply contains(String inputName){
		int supplyAmount = supplies.size();
		for(int i = 0; i < supplyAmount; i++){
			Supply supply = supplies.get(i);
			String supplyName = supply.getName();
			if(inputName == supplyName){
				return supply;
			}
		}
		return null;
	}
}


