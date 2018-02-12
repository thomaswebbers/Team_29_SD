package main.java.softdesign;


import simbad.gui.*;
import simbad.sim.*;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

/**
  Derivate your own code from this example.
 */


public class Main {

    public static void main(String[] args) {
        // request antialising so that diagonal lines are not "stairy"
        System.setProperty("j3d.implicitAntialiasing", "true");
        
        // creation of the environment containing all obstacles and robots
        EnvironmentDescription environment = new MyEnvironment();
        
        // crete two instances of the same example robot
        MyRobot robot1 = new MyRobot(new Vector3d(0, 0, 0), "Robot 1");
        
        // create all instances of cherry
        ArrayList<CherryAgent> cherryList = new ArrayList<CherryAgent>();
        
        for(int i = -4; i <= 4; i++){ //!better way to change interval
        	for(int j = -4; j <= 4; j++){
        		String cherryName = "Cherry("+i+", "+j+")";
        		if(false){
            		cherryList.add(new CherryAgent(new Vector3d(i, 0, j), cherryName, 0.1f));
        		}
        	}
        }
        
        // add cherries to environment
        for(int i = 0; i < cherryList.size(); i++){
        	System.out.println("added cherry: "+cherryList.get(i).getName());
        	cherryList.get(i).setCanBeTraversed(true);
        	environment.add(cherryList.get(i));
        }

        // add the two robots to the environment
        environment.add(robot1);
        //environment.add(robot2);
        
        
        // here we create an instance of the whole Simbad simulator and we assign the newly created environment 
        Simbad frame = new Simbad(environment, false);
        frame.update(frame.getGraphics());
    }

} 