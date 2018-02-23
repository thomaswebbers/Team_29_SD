package main.java.softdesign;

import java.awt.Color;
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import simbad.sim.Box;
import simbad.sim.CherryAgent;
import simbad.sim.EnvironmentDescription;
import simbad.sim.Wall;

public class MyEnvironment extends EnvironmentDescription {
	public MyEnvironment() {
		
		// turn on the lights
        this.light1IsOn = true;
        this.light2IsOn = true;
        
        this.showAxis(false);
        
        this.setWorldSize(14);
        
        Box w1 = new Box(new Vector3d(-5, 0, -0.5),new Vector3f(1, 1, 10), this);
        w1.setColor(new Color3f(Color.BLUE));
        add(w1);
        
        Box w2 = new Box(new Vector3d(5, 0, 0.5),new Vector3f(1, 1, 10), this);
        w2.setColor(new Color3f(Color.GREEN));
        add(w2);
        
        Box w3 = new Box(new Vector3d(-0.5, 0, 5),new Vector3f(10, 1, 1), this);
        w3.setColor(new Color3f(Color.RED));
        add(w3);
        
        Box w4 = new Box(new Vector3d(0.5, 0, -5),new Vector3f(10, 1, 1), this);
        w4.setColor(new Color3f(Color.YELLOW));
        add(w4);
        
        Box wallBox1 = new Box(new Vector3d (2, 0, -3), new Vector3f(1,1,4), this);
        wallBox1.setColor(new Color3f(Color.BLUE));
        add (wallBox1);
        
        Box wallBox2 = new Box(new Vector3d (3.25, 0, -1), new Vector3f(3.5f,1,1), this);
        wallBox2.setColor(new Color3f(Color.CYAN));
        add (wallBox2);
        
        Box box1 = new Box(new Vector3d(-3, 0, -3), new Vector3f(1, 1, 1), this);
        box1.setColor(new Color3f(Color.ORANGE));
        add(box1);
        
        Box box2 = new Box(new Vector3d(-3, 0, 3), new Vector3f(1, 1, 1), this);
        box2.setColor(new Color3f(Color.ORANGE));
        add(box2);
        
        Box box3 = new Box(new Vector3d(-2, 0, 3), new Vector3f(1,1,1), this);
        box3.setColor(new Color3f(Color.YELLOW));
        add(box3);
                
        //add robots to the environment (have to add here, or cherries won't work), TESTING MISSIONS ADDED HERE
        MyRobot r1 = new MyRobot(new Vector3d(0,0,0), "inky");
        r1.setColor(new Color3f(Color.BLUE));
        //TESTING make mission
        ArrayList<Vector3d> r1Mission = new ArrayList<Vector3d>();
        for(int i = 0; i <= 4; i++){
        	for(int j = 0; j <= 4; j++){
        		r1Mission.add(new Vector3d(i, 0, j));
        	}
        }
        r1.setMission(r1Mission);
        add(r1);
        
        MyRobot r2 = new MyRobot(new Vector3d(-1,0,0), "blinky");
        r2.setColor(new Color3f(Color.RED));
        //TESTING make mission
        ArrayList<Vector3d> r2Mission = new ArrayList<Vector3d>();
        for(int i = -4; i <= -1; i++){
        	for(int j = 0; j <= 4; j++){
        		r2Mission.add(new Vector3d(i, 0, j));
        	}
        }
        r2.setMission(r2Mission);
        add(r2);
        
        MyRobot r3 = new MyRobot(new Vector3d(-1,0,-1), "pinky");
        r3.setColor(new Color3f(Color.PINK));
        //TESTING make mission
        ArrayList<Vector3d> r3Mission = new ArrayList<Vector3d>();
        for(int i = -4; i <= -1; i++){
        	for(int j = -4; j <= -1; j++){
        		r3Mission.add(new Vector3d(i, 0, j));
        	}
        }
        r3.setMission(r3Mission);
        add(r3);
        
        MyRobot r4 = new MyRobot(new Vector3d(0,0,-1), "clyde");
        r4.setColor(new Color3f(Color.ORANGE));
        //TESTING make mission
        ArrayList<Vector3d> r4Mission = new ArrayList<Vector3d>();
        for(int i = 0; i <= 4; i++){
        	for(int j = -4; j <= -1; j++){
        		r4Mission.add(new Vector3d(i, 0, j));
        	}
        }
        r4.setMission(r4Mission);
        add(r4);
        
        // create all instances of cherry
        for(int i = -4; i <= 4; i++){
        	for(int j = -4; j <= 4; j++){
        		String cherryName = "Cherry("+i+", "+j+")";
        		if(Math.abs(i) <= 2 && Math.abs(j) <= 2){
        			continue;
        		}
        		if(true){//! CHANGE ME IF CRASHES DUE TO NULLPOINTER
                	add(new CherryAgent(new Vector3d(i, 0, j), cherryName, 0.15f));
        		}
        	}
        }
    }
}
