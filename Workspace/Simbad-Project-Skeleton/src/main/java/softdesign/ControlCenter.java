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
	private int connectedRobotAmount;

	public ReentrantLock lock;

	private ControlCenter(Vector3d position, String name) {
		super(position, name);

		// Initialise ControlCenter variables
		myMode = DeviceMode.Inactive;
		myStatus = UpdateStatus.Done;
		myMission = new Mission();
		myEnvironmentData = new EnvironmentData();
		myRobots = new ArrayList<Observer>();
		connectedRobotAmount = 0;
		lock = new ReentrantLock();
	}

	public void initBehavior() {
		System.out.println("I exist and my name is " + this.name);
		myMode = DeviceMode.Inactive;

	}

	/**
	 * This method is call cyclically (20 times per second) by the simulator
	 * engine.
	 */
	public void performBehavior() {		
		if(myMode == DeviceMode.Inactive){
			if(!myMission.isEmpty()){
				myMode = DeviceMode.Active;
				delegateMyMission();
			}else{
				return;
			}
		}else{
			if(myMission.isEmpty()){
				myMode = DeviceMode.Inactive;
				System.out.println("CC "+this.getName()+" done, shutting down");
				return;
			}
		}
		
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
				if(getCounter() >= lastUpdate + 20){
					//timeout of robots, go to next step;
					updatesReceived = connectedRobotAmount;
				}
				if (updatesReceived >= connectedRobotAmount) {
					myStatus = UpdateStatus.Sending;
					updatesSent = 0;
					updateRobots(UpdateStatus.Receiving);
				}
				// if we're done receiving check if all robots have gotten the updated map
			} else if (myStatus == UpdateStatus.Sending) {
				if(getCounter() >= lastUpdate + 40){
					//timeout of robots, go to next step;
					updatesReceived = connectedRobotAmount;
				}
				if (updatesSent >= connectedRobotAmount) {
					myStatus = UpdateStatus.Done;
					lastUpdate = getCounter();
					System.out.printf("Succesful update!\n");
					myMission.checkEnvironment(myEnvironmentData);
					myEnvironmentData.printEnvironment();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean delegateMyMission() {
		int connectedRobotAmount = myRobots.size();
		// if I have no robots, I can't delegate my mission
		if (connectedRobotAmount <= 0) {
			return false;
		}
		try {
			lock.lock();
			missionList = myMission.splitMission(connectedRobotAmount);
			for (int i = 0; i < connectedRobotAmount; i++) {
				Observer robot = myRobots.get(i);
				robot.updateMission(i);
			}
		} finally {
			lock.unlock();
		}

		return true;
	}

	public Mission getMissionNr(int missionNr) {
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
		for (int i = 0; i < connectedRobotAmount; i++) {
			Observer robot = myRobots.get(i);
			try{
				lock.lock();
				boolean connectionEstablished = robot.updateStatus(input);
				if(!connectionEstablished){
					connectedRobotAmount--;
				}
			}finally{
				lock.unlock();
			}
		}
	}

	public static ControlCenter getInstance() {
		return instance;
	}

	public Robot addRobot(String robotType, Vector3d pos, String robotName, Color3f robotColor) {
		RobotFactory robotFactory = RobotFactory.getInstance();
		Robot robot = robotFactory.getRobot(robotType, pos, robotName, robotColor);
		myRobots.add(robot);
		try{
			lock.lock();
			connectedRobotAmount++;
		}finally{
			lock.unlock();
		}
		return robot;
	}

	public boolean reassignMission(int robotMission, Mission input) {
		missionList.set(robotMission, input);
		for(Observer robot : myRobots){
			if(robot.updateMission(robotMission)){
				return true;
			}
		}
		System.out.println("CC "+this.getName()+" MISSION FAILED, ALL ROBOTS HAVE ERRORS");
		myMission = new Mission();
		return false;
	}

	public boolean acceptConnection() {
		try{
			lock.lock();
			connectedRobotAmount++;
		}finally{
			lock.unlock();
		}
		return true;
	}
}