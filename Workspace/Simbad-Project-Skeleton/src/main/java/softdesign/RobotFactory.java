package main.java.softdesign;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

public class RobotFactory {
	
	private static RobotFactory instance = new RobotFactory();
	
	private RobotFactory(){
		
	}
	
	public static RobotFactory getInstance(){
		return instance;
	}
	
	public Robot getRobot(String robotType, Vector3d pos, String name, Color3f robotColor){
		ControlCenter cc = ControlCenter.getInstance();
		
		if(robotType.equalsIgnoreCase("MYROBOT")){
			Robot robot = new MyRobot(pos, name);
			robot.setColor(robotColor);
			robot.setSupervisor(cc);
			robot.changeLock(cc.lock);
			System.out.printf("%s supervises: %s\n", cc.getName(), robot.getName());
			return robot;
		}
		return null;
	}
}
