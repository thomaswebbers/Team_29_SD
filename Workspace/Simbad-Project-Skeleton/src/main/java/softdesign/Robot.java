package main.java.softdesign;

import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;
import simbad.sim.Agent;


public interface Robot extends Device {

	public void setColor(Color3f color3f);

	public void getUpdate();

	public void sendUpdate();

	public void getSupervisorMission(int i);

	public void setSupervisor(ControlCenter controlCenter);

	public void changeLock(ReentrantLock lock);

	public String getName();
}
