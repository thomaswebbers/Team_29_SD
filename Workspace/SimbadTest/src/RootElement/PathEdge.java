package RootElement;

import javax.vecmath.Vector3d;

public class PathEdge {

	Vector3d from, to;
	
	public PathEdge(Vector3d from, Vector3d to){
		this.from = from;
		this.to = to;
	}
	
	Vector3d returnFrom(){
		return from;
	}
	
	Vector3d returnTo(){
		return to;
	}
	
	public String toString(){
		return "from: "+from.toString()+", to: "+to.toString();
	}
	
	//return 0 if it equals from, 1 if it equals to. Otherwise return negative number
	public int contains(Vector3d input){
		if(input.equals(from)){
			return 0;
		}
		if(input.equals(to)){
			return 1;
		}
		return -1;
	}
		
	@Override
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(!PathEdge.class.isAssignableFrom(obj.getClass())){
			return false;
		}
		
		PathEdge input = (PathEdge) obj;
		if(this.from.equals(input.from) && this.to.equals(input.to)){
			return true;
		}
		return false;
	}
}
