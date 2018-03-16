package main.java.softdesign;

import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import simbad.sim.Box;
import simbad.sim.EnvironmentDescription;

public class CollisionTester extends EnvironmentDescription {
	public CollisionTester() {
		// turn on the lights
		this.light1IsOn = true;
		this.light2IsOn = true;

		this.showAxis(false);

		this.setWorldSize(9);

		Box w1 = new Box(new Vector3d(-3, 0, -0.5), new Vector3f(1, 1, 6), this);
		w1.setColor(new Color3f(Color.BLUE));
		add(w1);

		Box w2 = new Box(new Vector3d(3, 0, 0.5), new Vector3f(1, 1, 6), this);
		w2.setColor(new Color3f(Color.GREEN));
		add(w2);

		Box w3 = new Box(new Vector3d(-0.5, 0, 3), new Vector3f(6, 1, 1), this);
		w3.setColor(new Color3f(Color.RED));
		add(w3);

		Box w4 = new Box(new Vector3d(0.5, 0, -3), new Vector3f(6, 1, 1), this);
		w4.setColor(new Color3f(Color.YELLOW));
		add(w4);
		
		Box b1 = new Box(new Vector3d(-1.5, 0, -1.5), new Vector3f(2,1,2), this);
		add(b1);

		// add robots to the environment
		MyRobot r1 = new MyRobot(new Vector3d(-2,0,0), "r1");
		MyRobot r2 = new MyRobot(new Vector3d(0,0,-2), "r2");
		ControlCenter cc = ControlCenter.getInstance();
		Lock lock = new ReentrantLock();
		
		Mission m1 = new Mission();
		m1.add(new Vector3d(2,0,0));
		r1.setMission(m1);
		r1.setSupervisor(cc);
		r1.changeLock(lock);
		
		Mission m2 = new Mission();
		m2.add(new Vector3d(0,0,2));
		r2.setMission(m2);
		r2.setSupervisor(cc);
		r2.changeLock(lock);
		
		add(r1);
		add(r2);
	}
}
