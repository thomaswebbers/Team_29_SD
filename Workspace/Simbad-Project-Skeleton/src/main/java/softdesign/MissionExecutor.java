package main.java.softdesign;

import javax.vecmath.Vector3d;

import simbad.sim.Agent;

public abstract class MissionExecutor extends Agent {

	protected UpdateStatus myStatus;
	protected DeviceMode myMode;
	protected Mission myMission;
	protected EnvironmentData myEnvironmentData;

	public MissionExecutor(Vector3d pos, String name) {
		super(pos, name);
	}

	public void setMission(Mission inputMission) {
		myMission = inputMission;
	}

	public Mission getMission() {
		return myMission;
	}
}
