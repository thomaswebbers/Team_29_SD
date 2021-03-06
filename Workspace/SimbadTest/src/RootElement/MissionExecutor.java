// --------------------------------------------------------
// Code generated by Papyrus Java
// --------------------------------------------------------

package RootElement;

import RootElement.DeviceMode;
import RootElement.EnvironmentData;
import RootElement.Mission;
import RootElement.UpdateStatus;
import java.lang.String;
import javax.vecmath.Vector3d;
import simbad.sim.Agent;

/************************************************************/
/**
 * 
 */
public abstract class MissionExecutor extends Agent {
	/**
	 * 
	 */
	protected UpdateStatus myStatus;
	/**
	 * 
	 */
	protected DeviceMode myMode;
	/**
	 * 
	 */
	protected Mission myMission;
	/**
	 * 
	 */
	protected EnvironmentData myEnvironmentData;

	/**
	 * 
	 * @param pos 
	 * @param name 
	 */
	public MissionExecutor(Vector3d pos, String name) {

		super(pos, name);
	}

	/**
	 * 
	 * @param inputMission 
	 */
	public void setMission(Mission inputMission) {

		myMission = inputMission;
	}
};
