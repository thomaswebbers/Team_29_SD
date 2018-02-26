package main.java.softdesign;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

public class Mission {
	private ArrayList<Vector3d> toVisit;
	
	public Mission(){
		toVisit = new ArrayList<Vector3d>();
	}
	
	public boolean setNewMission(ArrayList<Vector3d> newMission){
		if(newMission == null){
			return false;
		}
		toVisit = newMission;
		return true;
	}
	
	public ArrayList<Mission> splitMission(int splitAmount){
		ArrayList<Mission> result = new ArrayList<Mission>();
		if(toVisit == null || toVisit.size() == 0){
			return null;
		}
		
		//first determine the size of the square that covers all mission targets
		Vector3d currentTarget = toVisit.get(0);
		int targetAmount = toVisit.size();

		double topLeftX = currentTarget.getX();
		double topLeftZ = currentTarget.getZ();
		double bottomRightX = currentTarget.getX();
		double bottomRightZ = currentTarget.getZ();
		
		for(int i = 1; i < targetAmount; i++){
			currentTarget = toVisit.get(i);
			
			topLeftX = Math.max(topLeftX, currentTarget.getX());
			topLeftZ = Math.min(topLeftZ, currentTarget.getZ());
			
			bottomRightX = Math.min(bottomRightX, currentTarget.getX());
			bottomRightZ = Math.max(bottomRightZ, currentTarget.getZ());
		}
		
		//now divide this square into the correct amount of cells
		int rows, collumns, cells;
		double rootOfSplitAmount = Math.sqrt(splitAmount);
		double roundedRoot = Math.round(rootOfSplitAmount);
		
		if(rootOfSplitAmount > roundedRoot){
			rows = (int) roundedRoot;
			collumns = (int) (roundedRoot+1);
		}else{
			rows = (int) roundedRoot;
			collumns = (int) roundedRoot;
		}
		cells = rows*collumns;
		
		//initialize the new list of missions
		for(int i = 0; i < cells; i++){
			result.add(new Mission());
		}
		
		//divide mission targets over new mission list depending on which cell they fall in
		int targetRow, targetCollumn, targetCell, currentX, currentZ;
		double distanceToTopX, distanceToTopZ;
		
		double rowLength, collumnLength;
		
		rowLength = Math.abs(topLeftX - bottomRightX) / rows;
		collumnLength = Math.abs(topLeftZ - bottomRightZ) / collumns;
		
		for(int i = 0; i < targetAmount; i++){
			currentTarget = toVisit.get(i);
			currentX = (int) currentTarget.getX();
			currentZ = (int) currentTarget.getZ();
			
			distanceToTopX = Math.abs(topLeftX - currentX);
			distanceToTopZ = Math.abs(topLeftZ - currentZ);

			targetRow =  (int) (distanceToTopX / rowLength);
			targetCollumn = (int) (distanceToTopZ / collumnLength);
			//edge case, to prevent out of bounds at edges of search area
			if(targetRow >= rows){
				targetRow = rows-1;
			}
			if(targetCollumn >= collumns){
				targetCollumn = collumns - 1;
			}
			
			targetCell = (targetRow * collumns) + targetCollumn;
			
			result.get(targetCell).add(currentTarget);
		}
		
		//if needed, merge missions to return correct splitAmount
		int mergeCounter = 0;
		Mission merger, mergee; //merger, which merges. mergee, which will be merged
		while(result.size() != splitAmount){
			merger = result.get(mergeCounter);
			mergee = result.get(mergeCounter+1);
			
			merger.addAll(mergee);
			result.remove(mergee);
			
			mergeCounter++;
		}
		return result;
	}
	
	public boolean contains(Vector3d input){
		return toVisit.contains(input);
	}
	
	public boolean remove(Vector3d input){
		return toVisit.remove(input);
	}
	
	public Vector3d getTarget(){
		return toVisit.get(0);
	}
	
	public boolean add(Vector3d input){
		return toVisit.add(input);
	}
	
	public boolean isEmpty(){
		return toVisit.size() == 0;
	}
	
	public ArrayList<Vector3d> getAll(){
		return toVisit;
	}
	
	public boolean addAll(Mission input){
		ArrayList<Vector3d> inputMissions = input.getAll();
		return toVisit.addAll(inputMissions);
	}
	
	//Check if the environment has mission targets which are visited/obstacles/unreachable
	public void checkEnvironment(EnvironmentData inputEnvironment){
		int missionSize = toVisit.size();
		ArrayList<Vector3d> toRemove = new ArrayList<Vector3d>();
		int toRemoveSize = 0;
		
		ArrayList<Vector3d> visited = inputEnvironment.getVisited();
		ArrayList<Vector3d> obstacles = inputEnvironment.getObstacles();
		ArrayList<Vector3d> unreachable = inputEnvironment.getUnreachable();
		
		//Determine which can be removed from the mission target list
		for(int i = 0; i < missionSize; i++){
			Vector3d target = toVisit.get(i);
			if(visited.contains(target)|| obstacles.contains(target) || unreachable.contains(target)){
				toRemove.add(target);
				toRemoveSize++;
			}
		}
		
		//Remove the tagets alreadly checked
		for(int i = 0; i < toRemoveSize; i++){
			Vector3d target = toRemove.get(i);
			toVisit.remove(target);
		}
	}

}
