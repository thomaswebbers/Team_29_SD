package RootElement;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3d;

public class EdgeArray {

	ArrayList<PathEdge> myData; //! rename?
	
	public EdgeArray(){
		myData = new ArrayList<PathEdge>();
	}
	
	PathEdge edgeTo(Vector3d input){
		int dataLength = myData.size();
		for(int i = 0; i < dataLength; i++){
			PathEdge currentEdge = myData.get(i);
			if(currentEdge.contains(input) == 1){
				return currentEdge;
			}
		}
		return null;
	}
	
	ArrayList<Vector3d> getPathTo(Vector3d input){
		Vector3d lastNode = input;
		PathEdge lastEdge = edgeTo(lastNode);
		ArrayList<Vector3d> result = new ArrayList<Vector3d>();
		
		
		while(lastEdge != null){
			result.add(lastNode);
			lastNode = lastEdge.returnFrom();
			lastEdge = edgeTo(lastNode);
		}
		
		Collections.reverse(result);
		return result;
	}
	
	boolean addEdge(PathEdge input){
		return myData.add(input);
	}
	
	boolean removeEdge(PathEdge input){
		return myData.remove(input);
	}
}
