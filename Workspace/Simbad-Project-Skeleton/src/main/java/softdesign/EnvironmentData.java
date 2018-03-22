package main.java.softdesign;

import java.util.ArrayList;
import javax.vecmath.Vector3d;

public class EnvironmentData {
	private ArrayList<Vector3d> visited;
	private ArrayList<Vector3d> obstacles;
	private ArrayList<Vector3d> unreachable;
	private ArrayList<Image> imageList;

	public EnvironmentData() {
		// initialize object variables
		visited = new ArrayList<Vector3d>();
		obstacles = new ArrayList<Vector3d>();
		unreachable = new ArrayList<Vector3d>();
		imageList = new ArrayList<Image>();
	}

	public boolean isVisited(Vector3d input) {
		return visited.contains(input);
	}

	public boolean addVisited(Vector3d input) {
		return visited.add(input);
	}

	public boolean isObstacle(Vector3d input) {
		return obstacles.contains(input);
	}

	public boolean addObstacle(Vector3d input) {
		return obstacles.add(input);
	}

	public boolean addUnreachable(Vector3d input) {
		return unreachable.add(input);
	}

	public boolean isUnreachable(Vector3d input) {
		return unreachable.contains(input);
	}

	public ArrayList<Vector3d> getVisited() {
		return visited;
	}

	public ArrayList<Vector3d> getObstacles() {
		return obstacles;
	}

	public ArrayList<Vector3d> getUnreachable() {
		return unreachable;
	}

	public ArrayList<Image> getImages() {
		return imageList;
	}

	// merge data of the input environmentData
	public void mergeData(EnvironmentData inputData) {
		ArrayList<Vector3d> inputVisited = inputData.getVisited();
		int inputVisitedSize = inputVisited.size();
		for (int i = 0; i < inputVisitedSize; i++) {
			Vector3d inputNode = inputVisited.get(i);
			if (!isVisited(inputNode)) {
				addVisited(inputNode);
			}
		}

		ArrayList<Vector3d> inputObstacles = inputData.getObstacles();
		int inputObstaclesSize = inputObstacles.size();
		for (int i = 0; i < inputObstaclesSize; i++) {
			Vector3d inputNode = inputObstacles.get(i);
			if (!isObstacle(inputNode)) {
				addObstacle(inputNode);
			}
		}

		ArrayList<Vector3d> inputUnreachables = inputData.getUnreachable();
		int inputUnreachablesSize = inputUnreachables.size();
		for (int i = 0; i < inputUnreachablesSize; i++) {
			Vector3d inputNode = inputUnreachables.get(i);
			if (!isUnreachable(inputNode)) {
				addUnreachable(inputNode);
			}
		}
		
		ArrayList<Image> inputImages = inputData.getImages();
		int inputImagesSize = inputImages.size();
		for (int i = 0; i < inputImagesSize; i++) {
			Image inputNode = inputImages.get(i);
			if (!this.hasImage(inputNode)) {
				addImage(inputNode);
			}
		}
	}

	private boolean hasImage(Image input) {
		for (Image target : imageList) {
			if (target.equals(input)) {
				return true;
			}
		}
		return false;
	}

	public void printEnvironment() {
		if(this.isEmpty()){
			System.out.println("X");
			return;
		}
		
		//get top left & bottom right of all environmentData
		Double topLeftX = null, topLeftZ = null, bottomRightX = null, bottomRightZ = null;
		
		if(!visited.isEmpty()){ //TODO think of an efficent way not to copy code, if only this was C
			Vector3d visitedNode = visited.get(0);
			if(topLeftX == null){
				topLeftX = visitedNode.getX();
				topLeftZ = visitedNode.getZ();
				bottomRightX = visitedNode.getX();
				bottomRightZ = visitedNode.getZ();
			}
			for (int i = 0; i < visited.size(); i++) {
				visitedNode = visited.get(i);

				topLeftX = Math.max(topLeftX, visitedNode.getX());
				topLeftZ = Math.min(topLeftZ, visitedNode.getZ());

				bottomRightX = Math.min(bottomRightX, visitedNode.getX());
				bottomRightZ = Math.max(bottomRightZ, visitedNode.getZ());
			}
		}
		
		if(!obstacles.isEmpty()){
			Vector3d obstacleNode = obstacles.get(0);
			if(topLeftX == null){
				topLeftX = obstacleNode.getX();
				topLeftZ = obstacleNode.getZ();
				bottomRightX = obstacleNode.getX();
				bottomRightZ = obstacleNode.getZ();
			}
			for (int i = 0; i < obstacles.size(); i++) {
				obstacleNode = obstacles.get(i);

				topLeftX = Math.max(topLeftX, obstacleNode.getX());
				topLeftZ = Math.min(topLeftZ, obstacleNode.getZ());

				bottomRightX = Math.min(bottomRightX, obstacleNode.getX());
				bottomRightZ = Math.max(bottomRightZ, obstacleNode.getZ());
			}
		}
		if(!unreachable.isEmpty()){
			Vector3d unreachableNode = unreachable.get(0);
			if(topLeftX == null){
				topLeftX = unreachableNode.getX();
				topLeftZ = unreachableNode.getZ();
				bottomRightX = unreachableNode.getX();
				bottomRightZ = unreachableNode.getZ();
			}
			for (int i = 0; i < unreachable.size(); i++) {
				unreachableNode = unreachable.get(i);

				topLeftX = Math.max(topLeftX, unreachableNode.getX());
				topLeftZ = Math.min(topLeftZ, unreachableNode.getZ());

				bottomRightX = Math.min(bottomRightX, unreachableNode.getX());
				bottomRightZ = Math.max(bottomRightZ, unreachableNode.getZ());
			}
		}

		for (int i = topLeftX.intValue(); i >= bottomRightX; i--) {
			for (int j = topLeftZ.intValue(); j <= bottomRightZ; j++) {
				Vector3d currentNode = new Vector3d(i, 0, j);
				if (visited.contains(currentNode)) {
					System.out.printf("V ");
				} else if (obstacles.contains(currentNode)) {
					System.out.printf("O ");
				} else if (unreachable.contains(currentNode)) {
					System.out.printf("U ");
				} else {
					System.out.printf("X ");
				}
			}
			System.out.println();
		}
	}
	
	public boolean isEmpty(){
		return visited.isEmpty() && obstacles.isEmpty() && unreachable.isEmpty();
	}

	public boolean hasObstacle(ArrayList<Vector3d> myPath) {
		for (Vector3d target : myPath) {
			if (isObstacle(target)) {
				return true;
			}
		}
		return false;
	}

	public void addImage(Image input) {
		imageList.add(input);
	}

	public EnvironmentData copy() {
		EnvironmentData copy = new EnvironmentData();
		copy.mergeData(this);
		return copy;
	}
}
