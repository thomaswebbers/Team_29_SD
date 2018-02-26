package main.java.softdesign;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Vector3d;

import simbad.sim.Agent;
import simbad.sim.Lock;

public class ControlCenter extends Agent{
	
	private Mission myMission;
	public ArrayList<Mission> missionList;
	private EnvironmentData myEnvironmentData;
	private ArrayList<MyRobot> myRobots;
	private String currentMode;
	public ReentrantLock lock; //using reentrantlock because Lock constructor does not seem to be public in Simbad

	public ControlCenter(Vector3d position, String name){
        super(position, name);

		//initialize ControlCenter variables
		currentMode = "Inactive";
		myMission = new Mission();
		myEnvironmentData = new EnvironmentData();
		myRobots = new ArrayList<MyRobot>();
		lock = new ReentrantLock();
	}
	
    public void initBehavior() {
		//go back to start
		this.moveToStartPosition();
        System.out.println("I exist and my name is " + this.name);
        currentMode = "Inactive";
    }

    /** This method is call cyclically (20 times per second) by the simulator engine. */
    public void performBehavior() {
    	
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
				myRobots.get(i).getSupervisorMission(i);
			}
		} finally {
			lock.unlock();
		}
		
		return true;
	}
	
	public void addRobot(MyRobot input){
		try{
			lock.lock();
			myRobots.add(input);
			input.mySupervisor = this;
		} finally{
			lock.unlock();
		}
	}
}
