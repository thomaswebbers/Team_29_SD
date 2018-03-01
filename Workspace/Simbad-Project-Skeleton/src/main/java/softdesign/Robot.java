package main.java.softdesign;

import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;
import simbad.sim.Agent;


public interface Robot extends Device {

	void setColor(Color3f color3f);

	void getUpdate();

	void sendUpdate();

	void getSupervisorMission(int i);

	void setSupervisor(ControlCenter controlCenter);

	void changeLock(ReentrantLock lock);

	String getName();
}
