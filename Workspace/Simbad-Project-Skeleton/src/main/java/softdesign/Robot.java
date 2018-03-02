package main.java.softdesign;

import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Color3f;

public interface Robot extends Observer{

	public void setColor(Color3f color3f);

	public void setSupervisor(ControlCenter controlCenter);

	public void changeLock(ReentrantLock lock);

	public String getName();
}
