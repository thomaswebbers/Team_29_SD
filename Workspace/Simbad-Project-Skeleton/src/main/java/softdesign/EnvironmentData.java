package main.java.softdesign;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

public class EnvironmentData {
	public ArrayList<Vector3d> visited;
	public ArrayList<Vector3d> obstacles;
	public ArrayList<Vector3d> unreachable;
	public ArrayList<Image> imageList;
	
	public EnvironmentData(){
		//initialize object variables
		visited = new ArrayList<Vector3d>();
		obstacles = new ArrayList<Vector3d>();
		unreachable = new ArrayList<Vector3d>();
		imageList = new ArrayList<Image>();
	}
	
	boolean isVisited(Vector3d input){
		return visited.contains(input);
	}
	
	boolean addVisited(Vector3d input){
		return visited.add(input);
	}
	
	boolean isObstacle(Vector3d input){
		return obstacles.contains(input);
	}
	
	boolean addObstacle(Vector3d input){
		return obstacles.add(input);
	}
	
	boolean addUnreachable(Vector3d input){
		return unreachable.add(input);
	}
	
	boolean isUnreachable(Vector3d input){
		return unreachable.contains(input);
	}
}
