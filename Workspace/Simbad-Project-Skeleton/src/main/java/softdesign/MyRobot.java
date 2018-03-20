package main.java.softdesign;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

import javax.swing.JPanel;
import javax.vecmath.*;

import simbad.sim.CameraSensor;
import simbad.sim.CherryAgent;
import simbad.sim.RangeSensorBelt;
import simbad.sim.RobotFactory;
import simbad.sim.SimpleAgent;

public class MyRobot extends MissionExecutor implements Robot {

	private int SENSOR_AMOUNT = 12;
	private int NO_MISSION_AVAILABLE = -1;
	private int NO_TIMER = -1;

	private double currentAngle;

	private Lock lock;
	private boolean collisionResolved;

	private ControlCenter mySupervisor;
	private int supervisorMission;
	private int myMissionNr;
	private ErrorStatus myErrorStatus;
	private boolean errorHandled;

	private ArrayList<Vector3d> myPath;
	private Vector3d finalTarget;
	private Vector3d currentTarget;
	private Vector3d previousTarget;
	private Vector3d startLocation;

	private CameraSensor myCamera;
	private BufferedImage cameraImage;
	JPanel cameraPanel;

	RangeSensorBelt mySonarBelt;

	int waitTimer;
	Vector3d waitLocation;

	public MyRobot(Vector3d position, String name) {
		super(position, name);

		// Add sonars
		mySonarBelt = RobotFactory.addSonarBeltSensor(this, SENSOR_AMOUNT);

		// Add camera & prep camera UI
		myCamera = RobotFactory.addCameraSensor(this);
		cameraImage = myCamera.createCompatibleImage();
		cameraPanel = new ImagerPanel();
		Dimension dim = new Dimension(cameraImage.getWidth(), cameraImage.getHeight());
		cameraPanel.setPreferredSize(dim);
		cameraPanel.setMinimumSize(dim);
		setUIPanel(cameraPanel);

		// initialize environment information
		currentAngle = 0;
		myPath = new ArrayList<Vector3d>();
		myEnvironmentData = new EnvironmentData();
		myMission = new Mission();
		supervisorMission = NO_MISSION_AVAILABLE;
		myStatus = UpdateStatus.Done;
		collisionResolved = true;
		waitTimer = NO_TIMER;
		previousTarget = this.getLocation();
		myErrorStatus = ErrorStatus.NoErrors;
		errorHandled = false;
	}

	/** This method is called by the simulator engine on reset. */
	public void initBehavior() {
		// go back to start
		this.moveToStartPosition();
		System.out.println("I exist and my name is " + this.name);
		startLocation = this.getLocation();
		myEnvironmentData.addVisited(startLocation);
		takePicture();
		myMode = DeviceMode.Inactive;
	}

	/**
	 * This method is call cyclically (20 times per second) by the simulator
	 * engine.
	 */
	public void performBehavior() {
		if (mySupervisor == null) {
			return;
		}
						
		//edge case, doesn't work in initBehaviour because sensors don't have data yet
		if(this.getCounter() == 0){
			registerObstacles();
		}

		this.checkUpdateStatus();

		
		//simulate errors happening
		simulateErrors();
		
		//handle possible errors
		handleErrors();
		
		// check if there is a nearby agent
		if (anOtherAgentIsVeryNear() && myErrorStatus != ErrorStatus.IRError) {
			SimpleAgent nearAgent = getVeryNearAgent();
			//if agent is a cherry remove/detach that cherry
			if (nearAgent instanceof CherryAgent) {
				nearAgent.detach();
			}
			//if agent is a robot, resolve collision conflict
			try {
				lock.lock();
				if (nearAgent instanceof Robot && !collisionResolved) {
					Robot nearRobot = (Robot) nearAgent;
					collisionResolved = true;
					nearRobot.resolveCollision(this);
				}
			} finally {
				lock.unlock();
			}
		}

		// if I am on a timer wait until it passes
		if (waitTimer != NO_TIMER) {
			pointTowards(waitLocation);
			if (getDistance(this.getLocation(), waitLocation) < 0.1) {
				setSpeed(0);
			}
			waitTimer--;
			if (waitTimer != 0) {
				return;
			}
			waitTimer = NO_TIMER;
			setSpeed(0.5);
		}
		// make change to current mode if applicable
		if (myMode == DeviceMode.Inactive) {
			if (!myMission.isEmpty()) {
				previousTarget = getLocation();
				startMission(previousTarget);
			} else {
				setSpeed(0);
				return;
			}
		}else if(myMode == DeviceMode.Active){
			if(myMission.isEmpty()){
				setSpeed(0);
				System.out.println(this.getName() + " no mission, shutting down");
				myMode = DeviceMode.Inactive;
				return;
			}
		}

		// If I am currently less than 0.05 units away from my target
		if (getDistance(this.getLocation(), currentTarget) < 0.1) {
			// reset collision resolvement
			collisionResolved = false;

			// get next location and add current location to visited
			myMission.remove(currentTarget);
			if (!myEnvironmentData.isVisited(currentTarget)) {
				// new place visited, register possible obstacles and take picture
				registerObstacles();
				takePicture();
				myEnvironmentData.addVisited(currentTarget);
			}

			myPath.remove(currentTarget);
			previousTarget = currentTarget;

			// generate a new path if it is null, empty or blocked
			while (myPath == null || myPath.size() == 0 || myEnvironmentData.hasObstacle(myPath)) {
				if (myMission.isEmpty()) {
					// current mission is over, shutting down.
					setSpeed(0);
					//handle RadioError
					if(myErrorStatus == ErrorStatus.RadioError && myMode != DeviceMode.Returning){
						this.returnToStart();
						return;
					}
					//robot returned, fix robot;
					if(myMode == DeviceMode.Returning){
						if(myErrorStatus == ErrorStatus.RadioError){
							while(!this.reconnectToSupervisor());
							mySupervisor.updateEnvironmentData(myEnvironmentData);
						}
						myErrorStatus = ErrorStatus.NoErrors;
						errorHandled = false;
						System.out.println(this.getName()+" returned back to start and was fixed");
					}else{
						System.out.println(this.getName() + " visited everything, shutting down");
					}
					myMode = DeviceMode.Inactive;
					return;
				}
				// set new target of current mission
				finalTarget = myMission.getClosest(previousTarget);
				myPath = getPath(previousTarget, finalTarget);
				if (myPath == null) {
					// no path can be calculated, target is unreachable
					myEnvironmentData.addUnreachable(finalTarget);
					myMission.remove(finalTarget);
				}
			}
			currentTarget = myPath.get(0);
		}
		pointTowards(currentTarget);
	}

	private void handleErrors() {
		if(myErrorStatus == ErrorStatus.NoErrors || errorHandled == true){
			return;
		}else if(myErrorStatus == ErrorStatus.IRError){
			this.shutDown();
			
		}else if(myErrorStatus == ErrorStatus.RadioError){
			//handled automatically when the mission ends
			
		}else if(myErrorStatus == ErrorStatus.LocomotionError){
			this.shutDown();
			
		}else if(myErrorStatus == ErrorStatus.CameraError){
			mySupervisor.reassignMission(myMissionNr, myMission);
			myMission = new Mission();
			this.returnToStart();
			
		}else if(myErrorStatus == ErrorStatus.SensorError){
			mySupervisor.reassignMission(myMissionNr, myMission);
			myMission = new Mission();
			this.returnToStart();

		}
		System.out.println("ERROR FOUND at "+this.getName()+": "+myErrorStatus.toString());
		errorHandled = true;
	}

	private void returnToStart() {
		waitAt(30, previousTarget);
		myPath = getPath(previousTarget, startLocation);
		finalTarget = startLocation;
		if(myPath == null){
			setSpeed(0);
			return;
		}
		currentTarget = myPath.get(0);
		myMode = DeviceMode.Returning;
	}

	private void simulateErrors() {
		//if I have an error or are within fixing distance, don't create a new one
		if(myErrorStatus != ErrorStatus.NoErrors || getDistance(previousTarget, startLocation) < 0.1){
			return;
		}
		double random = Math.random();
		if(random < 0.0001){
			myErrorStatus = ErrorStatus.IRError;
		}else if(random < 0.0002){
			myErrorStatus = ErrorStatus.RadioError;
		}else if(random < 0.0003){
			this.setSpeed(0);
			myErrorStatus = ErrorStatus.LocomotionError;
		}else if(random < 0.0004){
			myErrorStatus = ErrorStatus.CameraError;
		}else if(random < 0.0005){ 
			myErrorStatus = ErrorStatus.SensorError;
		}
		if(myErrorStatus != ErrorStatus.NoErrors){
			System.out.println("Error generated at "+this.getName()+": "+myErrorStatus.toString());
		}
	}

	private void setSpeed(double d) {
		if(myErrorStatus == ErrorStatus.LocomotionError){
			return;
		}
		this.setTranslationalVelocity(d);
	}

	public void startMission(Vector3d startLocation) {
		myMission.checkEnvironment(myEnvironmentData);
		if(myMission.isEmpty()){
			return;
		}
		Vector3d newTarget = myMission.getClosest(startLocation);
		myPath = getPath(startLocation, newTarget);
		if(myPath == null){
			waitAt(30 ,startLocation);
			return;
		}
		currentTarget = myPath.get(0);
		pointTowards(currentTarget);
		
		myMode = DeviceMode.Active;
		setSpeed(0.5);
	}

	private void checkUpdateStatus() {
		if(myErrorStatus == ErrorStatus.RadioError){
			return;
		}
		try {
			lock.lock();
			if (supervisorMission != NO_MISSION_AVAILABLE) {
				myMission.addAll(mySupervisor.getMissionNr(supervisorMission));
				System.out.printf("%s accepted mission %d\n", this.getName(), supervisorMission);
				myMissionNr = supervisorMission;
				supervisorMission = NO_MISSION_AVAILABLE;
			}

			if (myStatus == UpdateStatus.Sending) {
				mySupervisor.updateEnvironmentData(myEnvironmentData);
				myStatus = UpdateStatus.Done;
			} else if (myStatus == UpdateStatus.Receiving) {
				myEnvironmentData = mySupervisor.sendEnvironmentData();
				myMission.checkEnvironment(myEnvironmentData);
			}
		} finally {
			lock.unlock();
		}
	}

	private double getDistance(Tuple3d from, Tuple3d to) {
		double legX, legZ, hypothenuse;
		legX = Math.abs(from.x - to.x);
		legZ = Math.abs(from.z - to.z);

		hypothenuse = Math.sqrt((legX * legX) + (legZ + legZ));
		return hypothenuse;
	}

	private Vector3d getLocation() {
		Point3d myPoint = new Point3d(0, 0, 0);
		this.getCoords(myPoint);
		Vector3d myLocation = new Vector3d(myPoint.getX(), 0, myPoint.getZ());
		return myLocation;
	}

	private ArrayList<Vector3d> getPath(Vector3d from, Vector3d to) {

		ArrayList<Vector3d> result = new ArrayList<Vector3d>();
		EdgeArray fromEdges = new EdgeArray();
		EdgeArray toEdges = new EdgeArray();

		// Edge case: from == to
		if (getDistance(from, to) < 1) {
			result.add(to);
			return result;
		}

		// Openvectors: vectors which don't have their outgoing edges added yet
		ArrayList<Vector3d> fromOpenVectors = new ArrayList<Vector3d>();
		ArrayList<Vector3d> toOpenVectors = new ArrayList<Vector3d>();
		// more efficient to start looking from both from & to at the same time
		fromOpenVectors.add(from);
		toOpenVectors.add(to);

		boolean pathFound = false;

		// while there are still vectors with possible new paths
		while (fromOpenVectors.size() > 0 && toOpenVectors.size() > 0 && !pathFound) {
			// Calculate new edges for open vectors of from
			int fromSize = fromOpenVectors.size();
			for (int i = 0; i < fromSize; i++) {
				// if a path has been found go to the main loop
				if (pathFound) {
					break;
				}
				// for every current open vector get all possible new paths/edges
				Vector3d openVector = fromOpenVectors.get(0);
				fromOpenVectors.remove(openVector);
				ArrayList<Vector3d> neighbours = getAdjacent(openVector);
				// check for all neighbours if there is already a path to them
				for (int j = 0; j < neighbours.size(); j++) {
					Vector3d neighbour = neighbours.get(j);
					if (fromEdges.edgeTo(neighbour) != null || neighbour.equals(from)) {
						// don't add an edge if there is already an edge to it
						continue;
					}
					if (myEnvironmentData.isObstacle(neighbour)) {
						// don't add new edge if neighbour is blocked.
						continue;
					}
					if (!myEnvironmentData.isVisited(neighbour) && myErrorStatus == ErrorStatus.SensorError){
						// don't go over unvisited nodes during errors
						continue;
					}
					if (toEdges.edgeTo(neighbour) != null) {
						// if toEdges has neighbour, a path has been found.
						result = fromEdges.getPathTo(openVector);
						ArrayList<Vector3d> toPath = toEdges.getPathTo(neighbour);
						Collections.reverse(toPath);
						result.addAll(toPath);
						result.add(to);
						pathFound = true;
						break;
					}
					if (neighbour.equals(to)) {
						// neighbour is endPoint
						result = fromEdges.getPathTo(openVector);
						result.add(neighbour);
						pathFound = true;
						break;
					}
					// if none of these things add the neighbour with a new edge & make open
					PathEdge newEdge = new PathEdge(openVector, neighbour);
					fromEdges.addEdge(newEdge);
					fromOpenVectors.add(neighbour);
				}
			}
			// Calculate new edges for open vectors of to
			int toSize = toOpenVectors.size();
			for (int i = 0; i < toSize; i++) {
				// if a path has been found go to the main loop
				if (pathFound) {
					break;
				}
				// for every current open vector get all possible new paths/edges
				Vector3d openVector = toOpenVectors.get(0);
				toOpenVectors.remove(openVector);
				ArrayList<Vector3d> neighbours = getAdjacent(openVector);
				// check for all neighbours if there is already a path to them
				for (int j = 0; j < neighbours.size(); j++) {
					Vector3d neighbour = neighbours.get(j);
					if (toEdges.edgeTo(neighbour) != null || neighbour.equals(to)) {
						// don't add an edge if there is already an edge to it
						continue;
					}
					if (myEnvironmentData.isObstacle(neighbour)) {
						// don't add new edge if neighbour is blocked.
						continue;
					}
					if (!myEnvironmentData.isVisited(neighbour) && myErrorStatus != ErrorStatus.NoErrors){
						// don't go over unvisited nodes during errors
						continue;
					}
					if (fromEdges.edgeTo(neighbour) != null) {
						// if fromEdges has neighbour, a path has been found.
						result = fromEdges.getPathTo(neighbour);
						ArrayList<Vector3d> toPath = toEdges.getPathTo(openVector);
						Collections.reverse(toPath);
						result.addAll(toPath);
						result.add(to);
						pathFound = true;
						break;
					}
					if (neighbour.equals(from)) {
						// neighbour is start point
						result.add(from);
						ArrayList<Vector3d> toPath = toEdges.getPathTo(openVector);
						Collections.reverse(toPath);
						result.addAll(toPath);
						pathFound = true;
						break;
					}
					// if none of these things add the neighbour with a new edge & make open
					PathEdge newEdge = new PathEdge(openVector, neighbour);
					toEdges.addEdge(newEdge);
					toOpenVectors.add(neighbour);
				}
			}
		}
		if (pathFound) {
			return result;
		}
		// no path was found;
		return null;

	}

	// returns neighbours starting from 1,0 in counter clock fashion
	private ArrayList<Vector3d> getAdjacent(Tuple3d input) {
		long inputX = Math.round(input.getX());
		long inputZ = Math.round(input.getZ());

		ArrayList<Vector3d> result = new ArrayList<Vector3d>();

		result.add(new Vector3d(inputX + 1, 0, inputZ));
		result.add(new Vector3d(inputX, 0, inputZ - 1));
		result.add(new Vector3d(inputX - 1, 0, inputZ));
		result.add(new Vector3d(inputX, 0, inputZ + 1));

		return result;
	}

	private boolean registerObstacles() {
		if(myErrorStatus == ErrorStatus.SensorError){
			return false;
		}
		boolean obstacleFound = false;

		// distance (radian) between sensors
		double sensorDistance = (Math.PI * 2) / SENSOR_AMOUNT;
		// the offset from sensor 0 to the northernmost sensor (sensor which points to x)
		int northOffset = (int) ((SENSOR_AMOUNT - Math.round(currentAngle / sensorDistance)) % SENSOR_AMOUNT);

		ArrayList<Vector3d> neighbours = getAdjacent(this.getLocation());

		double centerMeasurement, leftMeasurement, rightMeasurement;

		for (int i = 0; i < neighbours.size(); i++) {
			Vector3d neighbour = neighbours.get(i);

			leftMeasurement = mySonarBelt.getMeasurement(((i * 3) + northOffset + 1) % SENSOR_AMOUNT);
			centerMeasurement = mySonarBelt.getMeasurement(((i * 3) + northOffset) % SENSOR_AMOUNT);
			rightMeasurement = mySonarBelt.getMeasurement(((i * 3) + northOffset + 1) % SENSOR_AMOUNT);

			if (leftMeasurement <= 0.5 && centerMeasurement <= 0.5 && rightMeasurement <= 0.5 && !myEnvironmentData.isObstacle(neighbour)) {
				myEnvironmentData.addObstacle(neighbour);
				myMission.remove(neighbour);
				
				obstacleFound = true;
			}
		}
		return obstacleFound;
	}

	private void pointTowards(Vector3d input) {
		Vector3d currentPoint = this.getLocation();

		double inputX = input.x;
		double inputZ = input.z;

		double currentX = currentPoint.x;
		double currentZ = currentPoint.z;

		double adjacentSide = 0, oppositeSide = 0;

		double quadrantAngle = 0, targetAngle = 0, angleChange = 0;
		
		// top left quadrant
		if (currentZ >= inputZ && currentX < inputX) {
			adjacentSide = Math.abs(currentX - inputX);
			oppositeSide = Math.abs(currentZ - inputZ);

			quadrantAngle = Math.atan(oppositeSide / adjacentSide);
			targetAngle = quadrantAngle;
		}
		
		// bottom left quadrant
		else if (currentZ > inputZ && currentX >= inputX) {
			adjacentSide = Math.abs(currentZ - inputZ);
			oppositeSide = Math.abs(currentX - inputX);

			quadrantAngle = Math.atan(oppositeSide / adjacentSide);
			targetAngle = quadrantAngle + (Math.PI * 0.5);
		}

		// bottom right quadrant
		else if (currentZ <= inputZ && currentX > inputX) {
			adjacentSide = Math.abs(currentX - inputX);
			oppositeSide = Math.abs(currentZ - inputZ);

			quadrantAngle = Math.atan(oppositeSide / adjacentSide);
			targetAngle = quadrantAngle + (Math.PI);
		}

		// top right quadrant
		else if (currentZ < inputZ && currentX <= inputX) {
			adjacentSide = Math.abs(currentZ - inputZ);
			oppositeSide = Math.abs(currentX - inputX);

			quadrantAngle = Math.atan(oppositeSide / adjacentSide);
			targetAngle = quadrantAngle + (Math.PI * 1.5);
		}

		// target angle calculated, rotate robot to target angle.
		angleChange = targetAngle - currentAngle;
		this.rotateY(angleChange);
		currentAngle = targetAngle;
		return;
	}

	// set the supervisor for the robot
	public void setSupervisor(ControlCenter input) {
		mySupervisor = input;
	}

	// change the lock used by the robot
	public void changeLock(Lock input) {
		lock = input;
	}

	// tell the robot it needs to retrieve a mission at the supervisor. and at what index to find it.
	public boolean updateMission(int input) {
		if(myErrorStatus == ErrorStatus.NoErrors){
			supervisorMission = input;
			return true;
		}else{
			return false;
		}
	}

	public boolean updateStatus(UpdateStatus input) {
		if(myErrorStatus == ErrorStatus.RadioError){
			return false;
		}
		myStatus = input;
		return true;
	}
	
	private boolean reconnectToSupervisor(){
		if(mySupervisor.acceptConnection()){
			return true;
		}
		return false;
	}

	private void takePicture() {
		if(myErrorStatus == ErrorStatus.CameraError){
			return;
		}
		myCamera.copyVisionImage(cameraImage);
		Image image = new Image(cameraImage, this.currentAngle, this.getName(), getLocation());
		myEnvironmentData.addImage(image);
		cameraPanel.repaint();
	}

	class ImagerPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(cameraImage, 0, 0, this);
		}
	}

	@Override
	public void resolveCollision(Robot collider) {
		collisionResolved = true;
		System.out.println(this.getName() + " collisionWith " + collider.getName());

		Vector3d colliderLocation = collider.getPreviousTarget();
		ArrayList<Vector3d> colliderPath = collider.getCurrentPath();
		Mission colliderMission = collider.getMission();
		DeviceMode colliderMode = collider.getMode();
		// a robot is returning, indicating an error. Shut down both to prevent damage
		if(myMode == DeviceMode.Returning || colliderMode == DeviceMode.Returning){
			System.out.println("collisionType: robot mode is returning, shut down both");
			myMode = DeviceMode.Inactive;
			collider.setMode(DeviceMode.Inactive);
			if(myMode != DeviceMode.Returning){
				myErrorStatus = ErrorStatus.LocomotionError;
				this.shutDown();
			}
			if(colliderMode != DeviceMode.Returning){
				collider.setErrorStatus(ErrorStatus.LocomotionError);
				collider.shutDown();
			}
			return;
		}
		
		// I am inactive
		if (myMode == DeviceMode.Inactive) {
			System.out.println("collisionType: I am inactive");
			this.setMission(colliderMission);
			this.startMission(previousTarget);
			Mission newMission = new Mission();
			newMission.add(colliderLocation);
			collider.setMission(newMission);
			collider.startMission(colliderLocation);
			collider.waitAt(30, colliderLocation);
			return;
		}

		// collider is inactive
		if (colliderMode == DeviceMode.Inactive) {
			System.out.println("collisionType: collider inactive");
			collider.setMission(myMission);
			collider.startMission(colliderLocation);
			Mission newMission = new Mission();
			newMission.add(previousTarget);
			this.setMission(newMission);
			this.startMission(previousTarget);
			this.waitAt(30, previousTarget);
			return;
		}

		// collision between visited nodes = exchange mission
		if (previousTarget.equals(colliderPath.get(0)) || colliderLocation.equals(myPath.get(0))) {
			System.out.println("collisionType: between visited nodes");
			this.waitAt(20, previousTarget);
			collider.waitAt(20, colliderLocation);
			Mission temp = myMission.clone();
			this.setMission(colliderMission);
			collider.setMission(temp);
			this.startMission(previousTarget);
			collider.startMission(colliderLocation);
			return;
		}

		// collision on an unvisited node
		myEnvironmentData.addVisited(currentTarget);
		myMission.remove(currentTarget);
		colliderMission.remove(currentTarget);
		// last node of one robot = let that robot return to its last location
		if (myPath.size() == 1 || colliderPath.size() == 1) {
			System.out.println("collisionType: unvisited is my/collider finalTarget");
			this.waitAt(30, previousTarget);
			collider.waitAt(30, colliderLocation);
			return;
		}
		// both paths pass = exchange missions
		if (previousTarget.equals(colliderPath.get(1)) && colliderLocation.equals(myPath.get(1))) {
			System.out.println("collisionType: both paths pass");
			Mission temp = myMission.clone();
			this.setMission(colliderMission);
			collider.setMission(temp);
			this.startMission(previousTarget);
			collider.startMission(colliderLocation);
			this.waitAt(30, previousTarget);
			collider.waitAt(30, colliderLocation);
			return;
		}

		// one path passes
		// the collider's path passes mine
		if (previousTarget.equals(colliderPath.get(1))) {
			System.out.println("collisionType: collider's path passes");
			this.waitAt(20, previousTarget);
			collider.waitAt(100, colliderLocation);
		} else {
			// my path passes the collider, or no path passes
			System.out.println("collisionType: my path passes, or no path passes");
			collider.waitAt(20, colliderLocation);
			this.waitAt(100, previousTarget);
		}

	}

	@Override
	public Vector3d getPreviousTarget() {
		return previousTarget;
	}

	@Override
	public ArrayList<Vector3d> getCurrentPath() {
		return myPath;
	}

	@Override
	public DeviceMode getMode() {
		return myMode;
	}

	@Override
	public void waitAt(int tim, Vector3d loc) {
		waitTimer = tim;
		waitLocation = loc;
		pointTowards(waitLocation);
	}
	
	public ErrorStatus returnErrorStatus(){
		return myErrorStatus;
	}

	public void setMode(DeviceMode input) {
		myMode = input;
	}

	@Override
	public void shutDown() {
		System.out.println(this.getName()+" shutting down immediately");
		this.setSpeed(0);
		mySupervisor.reassignMission(myMissionNr, myMission);
		myMission = new Mission(); //make mission empty
		myEnvironmentData.addObstacle(previousTarget);
		myEnvironmentData.addObstacle(currentTarget);
	}

	@Override
	public void setErrorStatus(ErrorStatus input) {
		myErrorStatus = input;
	}
}
