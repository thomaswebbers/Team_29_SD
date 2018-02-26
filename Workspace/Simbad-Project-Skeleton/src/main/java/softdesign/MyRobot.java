package main.java.softdesign;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;
import javax.vecmath.*;

import simbad.sim.Agent;
import simbad.sim.CameraSensor;
import simbad.sim.CherryAgent;
import simbad.sim.RangeSensorBelt;
import simbad.sim.RobotFactory;
import simbad.sim.SensorMatrix;
import simbad.sim.SimpleAgent;

public class MyRobot extends Agent {
	
	private int SENSOR_AMOUNT = 12;
	private int NO_MISSION_AVAILABLE = -1;


	private DeviceMode currentMode;
	private double currentAngle;
	
	private EnvironmentData myEnvironmentData;
	private Mission myMission;
	
	//supervisor variables, always lock
	private ReentrantLock lock;
	private ControlCenter mySupervisor;
	private int supervisorMission;
	private UpdateStatus updateStatus;
	
	private ArrayList<Vector3d> myPath;
	private Vector3d finalTarget;
	private Vector3d currentTarget;
	private Vector3d previousTarget;
	
	private CameraSensor myCamera;
	SensorMatrix luminanceMatrix;
    JPanel cameraPanel;
    
    RangeSensorBelt mySonarBelt;
	
    public MyRobot(Vector3d position, String name) {
        super(position, name);
        
        // Add bumpers
        //RobotFactory.addBumperBeltSensor(this, 12); //! removed bumpers as they don't serve a purpose yet
        
        // Add sonars
        mySonarBelt = RobotFactory.addSonarBeltSensor(this, SENSOR_AMOUNT);  
        
        // Add camera & prep camera UI
        myCamera = RobotFactory.addCameraSensor(this);
        luminanceMatrix = myCamera.createCompatibleSensorMatrix();
        cameraPanel = new ImagerPanel();
        Dimension dim = new Dimension(luminanceMatrix.getWidth(), luminanceMatrix.getHeight());
        cameraPanel.setPreferredSize(dim);
        cameraPanel.setMinimumSize(dim);
        setUIPanel(cameraPanel);
        
        // initialize environment information
        currentAngle = 0;
        myPath = new ArrayList<Vector3d>();
        myEnvironmentData = new EnvironmentData();
        myMission = new Mission();
        supervisorMission = NO_MISSION_AVAILABLE;
        updateStatus = UpdateStatus.Done;
    }

    /** This method is called by the simulator engine on reset. */
    public void initBehavior() {
		//go back to start
		this.moveToStartPosition();
        System.out.println("I exist and my name is " + this.name);
        currentMode = DeviceMode.Inactive;
    }

    /** This method is call cyclically (20 times per second) by the simulator engine. */
    public void performBehavior() {
    	//check if the supervisor left a message
    	if(mySupervisor == null){
    		return;
    	}
    	try{
    		lock.lock();
    		if(supervisorMission != NO_MISSION_AVAILABLE){
    			myMission = mySupervisor.getMission(supervisorMission);
    			System.out.printf("%s accepted mission %d\n", this.getName(), supervisorMission);
    			supervisorMission = NO_MISSION_AVAILABLE;
    		}
    		
    		if(updateStatus == UpdateStatus.Sending){
    			mySupervisor.updateEnvironmentData(myEnvironmentData);
    			updateStatus = UpdateStatus.Done;
    		}else if(updateStatus == UpdateStatus.Receiving){
    			myEnvironmentData = mySupervisor.sendEnvironmentData();
    			myMission.checkEnvironment(myEnvironmentData);
    		}
    	} finally {
    		lock.unlock();
    	}
    	
    	//make change to current mode if applicable
    	if(currentMode == DeviceMode.Inactive){
    		if(!myMission.isEmpty()){
    			previousTarget = getLocation();
    			registerObstacles();
    			Vector3d newTarget = myMission.getTarget();
    	        myPath = getPath(previousTarget, newTarget);
    	        currentTarget = myPath.get(0);
    			currentMode = DeviceMode.Active;
    			setTranslationalVelocity(0.5);
    		}else{
    			setTranslationalVelocity(0);
        		return;	
    		}
    	}
    	
    	
    	//If I am touching a cherry, detach (delete) that cherry
		if(anOtherAgentIsVeryNear()){
			SimpleAgent nearAgent = getVeryNearAgent();
			if (nearAgent instanceof CherryAgent){
				nearAgent.detach();	
			}
		}
    	
    	if(getCounter() % 5 == 0){
    		//If I am currently less than 0.1 units away from my target
    		if (getDistance(this.getLocation(), currentTarget) < 0.1){    			    			
    			//take a picture and select a new target
    			myCamera.copyVisionImage(luminanceMatrix); //! store image in EnvironmentData
    			cameraPanel.repaint();
    			
    			//get next location and add current location to visited
    			myMission.remove(currentTarget);
    			if(!myEnvironmentData.isVisited(currentTarget)){    				
    				registerObstacles(); //new place visited, register possible obstacles
    				myEnvironmentData.addVisited(currentTarget);
    			}
    			
    			myPath.remove(currentTarget); //currentTarget should be at position 0 in the myPath array
    			previousTarget = currentTarget;
    			
    			//generate a new path if it is null, empty, or blocked
    			while(myPath == null || myPath.size() == 0 || myEnvironmentData.isObstacle(myPath.get(0))){
    				if(myMission.isEmpty()){
    					//current mission is over, shutting down.
    					setTranslationalVelocity(0);
    					System.out.println(this.getName() + " VISITED EVERYTHING, SHUTTING DOWN");
    					currentMode = DeviceMode.Inactive;
    					return;
    				}
    				//set new target of current mission
    				finalTarget = myMission.getTarget();
    				myPath = getPath(previousTarget, finalTarget);
    				if(myPath == null){
    					//add that it is unreachable
    					myEnvironmentData.addUnreachable(finalTarget);
    					myMission.remove(finalTarget);
    				}
    			}
        		currentTarget = myPath.get(0); 
    		}
    		
    		pointTowards(currentTarget);
    	}
    }
    
    private double getDistance(Tuple3d from, Tuple3d to){
    	double legX, legZ, hypothenuse;
    	legX = Math.abs(from.x - to.x);
    	legZ = Math.abs(from.z - to.z);
    	
    	hypothenuse = Math.sqrt((legX*legX) + (legZ+legZ));
    	return hypothenuse;
    }
    
    private Vector3d getLocation(){
    	Point3d myPoint = new Point3d(0,0,0);
    	this.getCoords(myPoint);
    	Vector3d myLocation = new Vector3d(myPoint.x, myPoint.y, myPoint.z);
    	return myLocation;
    }
    
    private ArrayList<Vector3d> getPath(Vector3d from, Vector3d to){
   
    	ArrayList<Vector3d> result = new ArrayList<Vector3d>();
    	EdgeArray fromEdges = new EdgeArray();
    	EdgeArray toEdges = new EdgeArray();

    	//Openvectors: vectors which don't have their outgoing edges added yet
    	ArrayList<Vector3d> fromOpenVectors = new ArrayList<Vector3d>();
    	ArrayList<Vector3d> toOpenVectors = new ArrayList<Vector3d>();
    	//more efficient to start looking from both from & to at the same time
    	fromOpenVectors.add(from);
    	toOpenVectors.add(to);
    	
    	boolean pathFound = false;

    	//while there are still vectors with possible new paths
    	while(fromOpenVectors.size() > 0 && toOpenVectors.size() > 0 && !pathFound){
    		//Calculate new edges for open vectors of from
    		int fromSize = fromOpenVectors.size();
    		for(int i = 0; i < fromSize; i++){
    			//if a path has been found go to the main loop
    			if(pathFound){
    				break;
    			}
    			//for every current open vector get all possible new paths/edges
    			Vector3d openVector = fromOpenVectors.get(0);
    			fromOpenVectors.remove(openVector);
    			ArrayList<Vector3d> neighbours = getAdjacent(openVector);
    			//check for all neighbours if there is already a path to them
    			for(int j = 0; j < neighbours.size(); j++){
    				Vector3d neighbour = neighbours.get(j);
    				if(fromEdges.edgeTo(neighbour) != null || neighbour.equals(from)){
    					//don't add an edge if there is already an edge to it
    					continue;
    				}
    				if(myEnvironmentData.isObstacle(neighbour)){
    					//don't add new edge if neighbour is blocked.
    					continue;
    				}
    				if(toEdges.edgeTo(neighbour) != null){
    					//if toEdges has neighbour, a path has been found.
    					result = fromEdges.getPathTo(openVector);
    					ArrayList<Vector3d> toPath = toEdges.getPathTo(neighbour);
    					Collections.reverse(toPath);
    					result.addAll(toPath);
    					result.add(to);
    					pathFound = true;
    					break;
    				}
    				if(neighbour.equals(to)){
    					//neighbour is endPoint
    					result = fromEdges.getPathTo(openVector);
    					result.add(neighbour);
    					pathFound = true;
    					break;
    				}
    				//if none of these things add the neighbour with a new edge & make open
    				PathEdge newEdge = new PathEdge(openVector, neighbour);
    				fromEdges.addEdge(newEdge);
    				fromOpenVectors.add(neighbour);
        		}
    		}
    		//Calculate new edges for open vectors of to
        	int toSize = toOpenVectors.size();
         	for(int i = 0; i < toSize; i++){
         		//if a path has been found go to the main loop
    			if(pathFound){
    				break;
    			}
    			//for every current open vector get all possible new paths/edges
    			Vector3d openVector = toOpenVectors.get(0);
    			toOpenVectors.remove(openVector);
    			ArrayList<Vector3d> neighbours = getAdjacent(openVector);
    			//check for all neighbours if there is already a path to them
    			for(int j = 0; j < neighbours.size(); j++){
    				Vector3d neighbour = neighbours.get(j);
    				if(toEdges.edgeTo(neighbour) != null || neighbour.equals(to)){
    					//don't add an edge if there is already an edge to it
    					continue;
    				}
    				if(myEnvironmentData.isObstacle(neighbour)){
    					//don't add new edge if neighbour is blocked.
    					continue;
    				}
    				if(fromEdges.edgeTo(neighbour) != null){
    					//if fromEdges has neighbour, a path has been found.
    					result = fromEdges.getPathTo(neighbour);
    					ArrayList<Vector3d> toPath = toEdges.getPathTo(openVector);
    					Collections.reverse(toPath);
    					result.addAll(toPath);
    					result.add(to);
    					pathFound = true;
    					break;
    				}
    				if(neighbour.equals(from)){
    					//neighbour is start point
    					result.add(from);
    					ArrayList<Vector3d> toPath = toEdges.getPathTo(openVector);
    					Collections.reverse(toPath);
    					result.addAll(toPath);
    					pathFound = true;
    					break;
    				}
    				//if none of these things add the neighbour with a new edge & make open
    				PathEdge newEdge = new PathEdge(openVector, neighbour);
    				toEdges.addEdge(newEdge);
    				toOpenVectors.add(neighbour);
        		}
    		}
    	}
    	if (pathFound){
    		return result;
    	}
		//no path was found;
		return null;

    }
    
    //returns neighbours starting from 1,0 in counter clock fashion
    private ArrayList<Vector3d> getAdjacent(Tuple3d input){
    	long inputX = Math.round(input.x);
    	long inputZ = Math.round(input.z);
    	    	
    	ArrayList<Vector3d> result = new ArrayList<Vector3d>();
    	
    	result.add(new Vector3d(inputX + 1, 0, inputZ));
    	result.add(new Vector3d(inputX, 0, inputZ - 1));
    	result.add(new Vector3d(inputX - 1 , 0, inputZ));
    	result.add(new Vector3d(inputX, 0, inputZ + 1));
    	
    	return result;
    }
    
    private boolean registerObstacles(){
    	boolean obstacleFound = false;
    	
    	//distance (radian) between sensors
    	double sensorDistance = (Math.PI*2)/SENSOR_AMOUNT;
    	//the offset from sensor 0 to the northernmost sensor (sensor which points to x)
    	int northOffset = (int) ((SENSOR_AMOUNT - Math.round(currentAngle/sensorDistance)) % SENSOR_AMOUNT);
    	
    	ArrayList<Vector3d> neighbours = getAdjacent(this.getLocation());
    	
    	double centerMeasurement, leftMeasurement, rightMeasurement;
    	
    	for(int i = 0; i < neighbours.size(); i++){
    		Vector3d neighbour = neighbours.get(i);
   
    		leftMeasurement = mySonarBelt.getMeasurement(((i*3) + northOffset + 1) % SENSOR_AMOUNT);
    		centerMeasurement = mySonarBelt.getMeasurement(((i*3) + northOffset) % SENSOR_AMOUNT);
    		rightMeasurement = mySonarBelt.getMeasurement(((i*3) + northOffset + 1) % SENSOR_AMOUNT);
    		
    		//0.4 because its the distance to the neighbour in the worst case scenario, a 45 degree angle
    		if(leftMeasurement <= 0.4 && centerMeasurement <= 0.4 && rightMeasurement <= 0.4 && !myEnvironmentData.isObstacle(neighbour)){
    			myEnvironmentData.addObstacle(neighbour);
    			myMission.remove(neighbour);
    			obstacleFound = true;
    		}
    	}
    	return obstacleFound;
    }
    
    public void setMission(Mission inputMission){
    	myMission = inputMission;
    }
    
    private void pointTowards(Vector3d input){
    	Vector3d currentPoint = this.getLocation();
    	
    	double inputX = input.x;
    	double inputZ = input.z;
    	
    	double currentX = currentPoint.x;
    	double currentZ = currentPoint.z;
    	
    	double adjacentSide = 0, oppositeSide = 0;
    	
    	double quadrantAngle = 0, targetAngle = 0, angleChange = 0;
    	    	
    	if(currentZ >= inputZ && currentX < inputX){ //top left quadrant
    		adjacentSide = Math.abs(currentX - inputX);
    		oppositeSide = Math.abs(currentZ - inputZ);
    		
    		quadrantAngle = Math.atan(oppositeSide/adjacentSide);
    		targetAngle = quadrantAngle;
    	}
    	
    	else if(currentZ > inputZ && currentX >= inputX){ //bottom left quadrant
    		adjacentSide = Math.abs(currentZ - inputZ);
    		oppositeSide = Math.abs(currentX - inputX);
    		
    		quadrantAngle = Math.atan(oppositeSide/adjacentSide);
    		targetAngle = quadrantAngle + (Math.PI * 0.5);
    	}
    	
    	else if(currentZ <= inputZ && currentX > inputX){ //bottom right quadrant
    		adjacentSide = Math.abs(currentX - inputX);
    		oppositeSide = Math.abs(currentZ - inputZ);
    		
    		quadrantAngle = Math.atan(oppositeSide/adjacentSide);
    		targetAngle = quadrantAngle + (Math.PI);
    	}
    	
    	else if(currentZ < inputZ && currentX <= inputX){//top right quadrant
    		adjacentSide = Math.abs(currentZ - inputZ);
    		oppositeSide = Math.abs(currentX - inputX);
    		
    		quadrantAngle = Math.atan(oppositeSide/adjacentSide);
    		targetAngle = quadrantAngle + (Math.PI * 1.5);
    	}
    	

		//target angle calculated, rotate robot to target angle.
		angleChange = targetAngle - currentAngle;
		this.rotateY(angleChange);
		currentAngle = targetAngle;
    	return;
    }
    
    //set the supervisor for the robot
    public void setSupervisor(ControlCenter input){
    	mySupervisor = input;
    }
    
    //change the lock used by the robot, SHOULD ALWAYS BE THE SAME LOCK AS ITS SUPERVISOR. Change to make this always the case?
    public void changeLock(ReentrantLock input){
    	lock = input;
    }
    
    //tell the robot it needs to retrieve a mission at the supervisor. and at what index to find it.
    public void getSupervisorMission(int input){
    	supervisorMission = input;
    }
    
    public void getUpdate(){
    	updateStatus = UpdateStatus.Receiving;
    }
    
    public void sendUpdate(){
    	updateStatus = UpdateStatus.Sending;
    }
    
    class ImagerPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
            int width = luminanceMatrix.getWidth();
            int height = luminanceMatrix.getHeight();
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    float level = luminanceMatrix.get(x, y);
                    if (level < 0.5) {
                        g.fillRect(x, y, 4, 4);
                    }
                }
            }

        }
    }
}
