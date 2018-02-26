package main.java.softdesign;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

public class EnvironmentData {
	private ArrayList<Vector3d> visited;
	private ArrayList<Vector3d> obstacles;
	private ArrayList<Vector3d> unreachable;
	private ArrayList<Image> imageList;
	
	public EnvironmentData(){
		//initialize object variables
		visited = new ArrayList<Vector3d>();
		obstacles = new ArrayList<Vector3d>();
		unreachable = new ArrayList<Vector3d>();
		imageList = new ArrayList<Image>();
	}
	
	public boolean isVisited(Vector3d input){
		return visited.contains(input);
	}
	
	public boolean addVisited(Vector3d input){
		return visited.add(input);
	}
	
	public boolean isObstacle(Vector3d input){
		return obstacles.contains(input);
	}
	
	public boolean addObstacle(Vector3d input){
		return obstacles.add(input);
	}
	
	public boolean addUnreachable(Vector3d input){
		return unreachable.add(input);
	}
	
	public boolean isUnreachable(Vector3d input){
		return unreachable.contains(input);
	}
	
	public ArrayList<Vector3d> getVisited(){
		return visited;
	}
	
	public ArrayList<Vector3d> getObstacles(){
		return obstacles;
	}
	
	public ArrayList<Vector3d> getUnreachable(){
		return unreachable;
	}
	
	public ArrayList<Image> getImages(){
		return imageList;
	}
	
	//merge data of the input environmentData
	public void mergeData(EnvironmentData inputData){
		ArrayList<Vector3d>inputVisited = inputData.getVisited();
		int inputVisitedSize = inputVisited.size();
		for(int i = 0; i < inputVisitedSize; i++){
			Vector3d inputNode = inputVisited.get(i);
			if(!isVisited(inputNode)){
				addVisited(inputNode);
			}
		}
		
		ArrayList<Vector3d>inputObstacles = inputData.getObstacles();
		int inputObstaclesSize = inputObstacles.size();
		for(int i = 0; i < inputObstaclesSize; i++){
			Vector3d inputNode = inputObstacles.get(i);
			if(!isObstacle(inputNode)){
				addObstacle(inputNode);
			}
		}
		
		ArrayList<Vector3d>inputUnreachables = inputData.getUnreachable();
		int inputUnreachablesSize = inputUnreachables.size();
		for(int i = 0; i < inputUnreachablesSize; i++){
			Vector3d inputNode = inputUnreachables.get(i);
			if(!isUnreachable(inputNode)){
				addUnreachable(inputNode);
			}
		}
		imageList.addAll(inputData.getImages());
	}
	
	//for now only capable top left to bottom right, IMPLEMENT OTHER SCENARIO'S
	public void printEnvironment(Vector3d from, Vector3d to){
		int fromX = (int) from.getX();
		int fromZ = (int) from.getZ();
		int toX = (int) to.getX();
		int toZ = (int) to.getZ();
		
		for(int i = fromX; i >= toX; i--){
			for(int j = fromZ; j <= toZ; j++){
				Vector3d currentNode = new Vector3d(i, 0, j);
				if(visited.contains(currentNode)){
					System.out.printf("V ");
				}else if(obstacles.contains(currentNode)){
					System.out.printf("O ");
				}else if(unreachable.contains(currentNode)){
					System.out.printf("U ");
				}else{
					System.out.printf("X ");
				}
			}
			System.out.println();
		}
	}
}
