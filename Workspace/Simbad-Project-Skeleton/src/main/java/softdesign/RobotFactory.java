package main.java.softdesign;

import javax.vecmath.Vector3d;

public class RobotFactory {
	
	private static RobotFactory instance = new RobotFactory();
	
	private RobotFactory(){
		
	}
	
	public static RobotFactory getinstance(){
		return instance;
	}
	
	public Robot getRobot(String robotType, Vector3d pos, String name){
		if(robotType.equalsIgnoreCase("MYROBOT")){
			return new MyRobot(pos, name);
		}
		return null;
	}
}
