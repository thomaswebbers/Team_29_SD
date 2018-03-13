package main.java.softdesign;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

public interface Robot extends Observer{

	public void setColor(Color3f color3f);

	public void setSupervisor(ControlCenter controlCenter);

	public void changeLock(ReentrantLock lock);

	public String getName();

	public void resolveCollision(Robot collider);

	public Vector3d getPreviousTarget();

	public ArrayList<Vector3d> getCurrentPath();

	public void setMission(Mission myMission);

	public Mission getMission();

	public void startMission(Vector3d startLocation);

	public DeviceMode getMode();

	public void waitAt(int tim, Vector3d loc);
}
