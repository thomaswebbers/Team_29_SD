package main.java.softdesign;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

public class ControlCenter extends MissionExecutor {
	private static ControlCenter instance = new ControlCenter(new Vector3d(0, 1, 0), "KingBoo");

	private int lastUpdate;
	private int updatesReceived;
	private int updatesSent;

	private ArrayList<Mission> missionList;

	private ArrayList<Observer> myRobots;
	private int robotAmount;

	public ReentrantLock lock;

	private ControlCenter(Vector3d position, String name) {
		super(position, name);

		// Initialise ControlCenter variables
		myMode = DeviceMode.Inactive;
		myStatus = UpdateStatus.Done;
		myMission = new Mission();
		myEnvironmentData = new EnvironmentData();
		myRobots = new ArrayList<Observer>();
		robotAmount = 0;
		lock = new ReentrantLock();
	}

	public void initBehavior() {
		System.out.println("I exist and my name is " + this.name);
		myMode = DeviceMode.Active;
		delegateMyMission();

	}

	/**
	 * This method is call cyclically (20 times per second) by the simulator
	 * engine.
	 */
	public void performBehavior() {
		try {
			lock.lock();
			// if it has been 3 seconds since the last finished update start a new one
			if (getCounter() >= lastUpdate + 60 && myStatus == UpdateStatus.Done) {
				System.out.printf("Update Started!\n");
				myStatus = UpdateStatus.Receiving;
				updatesReceived = 0;
				updateRobots(UpdateStatus.Sending);
				// if we're still receiving, check if all robots have sent their map yet
			} else if (myStatus == UpdateStatus.Receiving) {
				if (updatesReceived >= robotAmount) {
					myStatus = UpdateStatus.Sending;
					updatesSent = 0;
					updateRobots(UpdateStatus.Receiving);
				}
				// if we're done receiving check if all robots have gotten the updated map
			} else if (myStatus == UpdateStatus.Sending) {
				if (updatesSent >= robotAmount) {
					myStatus = UpdateStatus.Done;
					lastUpdate = getCounter();
					System.out.printf("Succesful update!\n");
					myEnvironmentData.printEnvironment(new Vector3d(5, 0, -5), new Vector3d(-5, 0, 5));
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean delegateMyMission() {
		int robotAmount = myRobots.size();
		// if I have no robots, I can't delegate my mission
		if (robotAmount <= 0) {
			return false;
		}
		try {
			lock.lock();
			missionList = myMission.splitMission(robotAmount);
			for (int i = 0; i < robotAmount; i++) {
				Observer robot = myRobots.get(i);
				robot.updateMission(i);
			}
		} finally {
			lock.unlock();
		}

		return true;
	}

	public Mission getMission(int missionNr) {
		return missionList.get(missionNr);
	}

	public void updateEnvironmentData(EnvironmentData input) {
		updatesReceived++;
		myEnvironmentData.mergeData(input);
	}

	public EnvironmentData sendEnvironmentData() {
		updatesSent++;
		return myEnvironmentData;
	}

	private void updateRobots(UpdateStatus input) {
		for (int i = 0; i < robotAmount; i++) {
			Observer robot = myRobots.get(i);
			robot.updateStatus(input);
		}
	}

	public static ControlCenter getInstance() {
		return instance;
	}

	public Robot addRobot(String robotType, Vector3d pos, String robotName, Color3f robotColor) {
		RobotFactory robotFactory = RobotFactory.getInstance();
		Robot robot = robotFactory.getRobot(robotType, pos, robotName, robotColor);
		myRobots.add(robot);
		robotAmount++;
		return robot;
	}
}