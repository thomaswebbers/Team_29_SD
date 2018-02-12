package main.java.softdesign;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.vecmath.*;

import simbad.sim.Agent;
import simbad.sim.CameraSensor;
import simbad.sim.CherryAgent;
import simbad.sim.RobotFactory;
import simbad.sim.SensorMatrix;

public class MyRobot extends Agent {

	private String currentMode;
	private double currentAngle;
	
	public ArrayList<Vector3d> blockedLocations;
	private ArrayList<Vector3d> myPath; //use as queue
	private Vector3d currentTarget;
	private Vector3d previousTarget;
	
	//start: camera
	private CameraSensor myCamera;
	SensorMatrix luminanceMatrix;
    JPanel cameraPanel;
	//end: camera
	
    public MyRobot(Vector3d position, String name) {
        super(position, name);
        
        // Add bumpers
        RobotFactory.addBumperBeltSensor(this, 12);
        // Add sonars
        RobotFactory.addSonarBeltSensor(this, 4);  
        // Add camera & prep camera UI
        myCamera = RobotFactory.addCameraSensor(this);
        luminanceMatrix = myCamera.createCompatibleSensorMatrix();
        cameraPanel = new ImagerPanel();
        Dimension dim = new Dimension(luminanceMatrix.getWidth(), luminanceMatrix.getHeight());
        cameraPanel.setPreferredSize(dim);
        cameraPanel.setMinimumSize(dim);
        setUIPanel(cameraPanel);
    }

    /** This method is called by the simulator engine on reset. */
    public void initBehavior() {
        System.out.println("I exist and my name is " + this.name);
        // initialize angle & path;
        currentAngle = 0;
        myPath = new ArrayList<Vector3d>();
        blockedLocations = new ArrayList<Vector3d>();
        
        //TESTING make test path & blocked locations
        for(int i = -4; i <= 4; i++){
        	for(int j = -4; j <= 4; j++){
        		myPath.add(new Vector3d(i, 0, j));
        	}
        }
        
        blockedLocations.add(new Vector3d(-4,0,-3));
        blockedLocations.add(new Vector3d(-3,0,-3));
        
        //TESTING? set current target
        currentTarget = myPath.get(0);
        
        //start moving
		setTranslationalVelocity(1);

    }

    /** This method is call cyclically (20 times per second) by the simulator engine. */
    public void performBehavior() {
    	if(getCounter() % 5 == 0){
    		//If I am touching a cherry, detach (delete) that cherry //!!! does not work yet due to cherry problem
    		if(getVeryNearAgent() instanceof CherryAgent){
    			CherryAgent foundCherry = (CherryAgent) getVeryNearAgent();
    			String foundCherryName = foundCherry.getName();
    			
    			System.out.printf("Found cherry %s\n", foundCherryName);
    			getVeryNearAgent().detach();
    		}
    		
    		//If I am currently less than 0.5 units away from my target
    		if (getDistance(this.getLocation(), currentTarget) < 0.5){
    			System.out.printf("arrived at: %.1f, %.1f\n", currentTarget.x, currentTarget.z);
    			
    			//TESTING calculate path to center
    			Vector3d center = new Vector3d(0,0,0);
    			    			
    			ArrayList<Vector3d> centerPath = getPath(currentTarget, center);
    			for(int i = 0; i < centerPath.size(); i++){
    				Vector3d vect = centerPath.get(i);
    				System.out.printf("PATH: (%.1f, %.1f)\n", vect.x, vect.z);
    			}
    			System.out.println();
    			
    			//take a picture and select a new target
    			myCamera.copyVisionImage(luminanceMatrix);
    			cameraPanel.repaint();
    			
    			myPath.remove(0);
    			previousTarget = currentTarget;
    			if(myPath.size() == 0){
    				System.out.println("MYPATH EMPTY");
    				System.exit(0);
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
    				if(blockedLocations.contains(neighbour)){
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
    				if(blockedLocations.contains(neighbour)){
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
    
    private ArrayList<Vector3d> getAdjacent(Tuple3d input){
    	double inputX = Math.round(input.x);
    	double inputZ = Math.round(input.z);
    	
    	ArrayList<Vector3d> result = new ArrayList<Vector3d>();
    	result.add(new Vector3d(input.x + 1, 0, input.z));
    	result.add(new Vector3d(input.x - 1, 0, input.z));
    	result.add(new Vector3d(input.x, 0, input.z + 1));
    	result.add(new Vector3d(input.x, 0, input.z - 1));
    	
    	return result;
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
