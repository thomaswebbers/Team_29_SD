package main.java.softdesign;

import javax.vecmath.Vector3d;

import simbad.sim.SensorMatrix;

public class Image {
	private SensorMatrix imageData;
	private double direction; // radians from positive X counter clockwise
	private String originName;
	private Vector3d location;

	public Image(SensorMatrix imageData, double direction, String originName, Vector3d location) {
		this.imageData = imageData;
		this.direction = direction;
		this.originName = originName;
		this.location = location;
	}

	public SensorMatrix getImageData() {
		return imageData;
	}

	public double getDirection() {
		return direction;
	}

	public String getOrigin() {
		return originName;
	}

	public Vector3d getLocation() {
		return location;
	}
}
