package main.java.softdesign;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Vector3d;

import simbad.sim.Agent;

public class ControlCenter extends Agent implements Device{
	private static ControlCenter instance = new ControlCenter(new Vector3d(0,1,0), "KingBoo"); //Singleton
	
	private UpdateStatus updateStatus;
	private int lastUpdate;
	private int updatesReceived;
	private int updatesSent;
	
	private DeviceMode currentMode;
	
	private Mission myMission;
	private ArrayList<Mission> missionList;
	
	private EnvironmentData myEnvironmentData;
	
	private ArrayList<Robot> myRobots;
	private int robotAmount;
	
	public ReentrantLock lock; //using reentrantlock because Lock constructor does not seem to be public in Simbad

	private ControlCenter(Vector3d position, String name){
        super(position, name);

		//Initialise ControlCenter variables
		currentMode = DeviceMode.Inactive;
		updateStatus = UpdateStatus.Done;
		myMission = new Mission();
		myEnvironmentData = new EnvironmentData();		
		myRobots = new ArrayList<Robot>();
		robotAmount = 0;
		lock = new ReentrantLock();
	}
	
    public void initBehavior() {
        System.out.println("I exist and my name is " + this.name);
        currentMode = DeviceMode.Active;
        delegateMyMission();

    }

    /** This method is call cyclically (20 times per second) by the simulator engine. */
    public void performBehavior() {
    	try{
    		lock.lock();
	    	//if it has been 3 seconds since the last finished update start a new one
	    	if(getCounter() >= lastUpdate+60 && updateStatus == UpdateStatus.Done){
    			System.out.printf("Update Started!\n");
	    		updateStatus = UpdateStatus.Receiving;
	    		updatesReceived = 0;
	    		for(int i = 0; i < robotAmount; i++){
	    			Robot robot = myRobots.get(i);
	    			robot.sendUpdate();
	    		}
	    	//if we're still receiving, check if all robots have sent their map yet.
	    	}else if(updateStatus == UpdateStatus.Receiving){
	    		if(updatesReceived >= robotAmount){
	    			updateStatus = UpdateStatus.Sending;
	    			updatesSent = 0;
	    			for(int i = 0; i < robotAmount; i++){
		    			Robot robot = myRobots.get(i);
		    			robot.getUpdate();
		    		}
	    		}
	    	//if we're done receiving check if all robots have gotten the updated map
	    	}else if(updateStatus == UpdateStatus.Sending){
	    		if(updatesSent >= robotAmount){
	    			updateStatus = UpdateStatus.Done;
	    			lastUpdate = getCounter();
	    			System.out.printf("Succesful update!\n");
	    			myEnvironmentData.printEnvironment(new Vector3d(5,0,-5), new Vector3d(-5,0,5));
	    		}
	    	}
    	} finally {
    		lock.unlock();
    	}
    }
    
	public void setMission(Mission inputMission){
		myMission = inputMission;
	}
	
	public boolean delegateMyMission(){
		int robotAmount = myRobots.size();
		//if I have no robots, I can't delegate my mission
		if(robotAmount <= 0){
			return false;
		}
		try{
			lock.lock();
			missionList = myMission.splitMission(robotAmount);
			for(int i = 0; i < robotAmount; i++){
				Robot robot = myRobots.get(i);
				robot.getSupervisorMission(i);
			}
		} finally {
			lock.unlock();
		}
		
		return true;
	}
	
	public Mission getMission(int missionNr){
		return missionList.get(missionNr);
	}
	
	public void updateEnvironmentData(EnvironmentData input){
		updatesReceived++;
		myEnvironmentData.mergeData(input);
	}
	
	public EnvironmentData sendEnvironmentData(){
		updatesSent++;
		return myEnvironmentData;
	}
	
	public static ControlCenter getInstance(){
		return instance;
	}
	
	//set a robot under the supervision of this supervisor, and make sure it's using the same lock
	public void addRobot(Robot input){
		try{
			lock.lock();
			myRobots.add(input);
			input.setSupervisor(this);
			input.changeLock(lock);
			robotAmount++;
			System.out.printf("%s supervises: %s\n", this.getName(), input.getName());
		} finally{
			lock.unlock();
		}
	}
}