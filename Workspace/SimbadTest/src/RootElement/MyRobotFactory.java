// --------------------------------------------------------
// Code generated by Papyrus Java
// --------------------------------------------------------

package RootElement;

import RootElement.Robot;
import java.lang.String;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

/************************************************************/
/**
 * 
 */
public class MyRobotFactory {
	/**
	 * 
	 */
	private static RootElement.MyRobotFactory instance = new MyRobotFactory();

	/**
	 * 
	 */
	private MyRobotFactory() {

	}

	/**
	 * 
	 * @return 
	 */
	public static RootElement.MyRobotFactory getInstance() {
		
		    return instance;
	}

	/**
	 * 
	 * @return 
	 * @param robotType 
	 * @param pos 
	 * @param name 
	 * @param robotColor 
	 */
	public Robot getRobot(String robotType, Vector3d pos, String name, Color3f robotColor) {

		ControlCenter cc = ControlCenter.getInstance();
		if (robotType.equalsIgnoreCase("MYROBOT")) {
			Robot robot = new MyRobot(pos, name);
			robot.setColor(robotColor);
			robot.setSupervisor(cc);
			robot.changeLock(cc.lock);
			System.out.printf("%s supervises: %s\n", cc.getName(), robot.getName());
			return robot;
		}
		return null;
	}
};
