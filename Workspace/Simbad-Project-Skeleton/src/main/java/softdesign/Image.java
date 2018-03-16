package main.java.softdesign;

import java.awt.image.BufferedImage;

import javax.vecmath.Vector3d;


public class Image {
	private BufferedImage imageData;
	private double direction; // radians from positive X counter clockwise
	private String originName;
	private Vector3d location;

	public Image(BufferedImage cameraImage, double direction, String originName, Vector3d location) {
		this.imageData = cameraImage;
		this.direction = direction;
		this.originName = originName;
		this.location = location;
	}

	public BufferedImage getImageData() {
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
	
	@Override
	public boolean equals(Object input){
		if(!(input instanceof Image)){
			return false;
		}
		Image inputImage = (Image) input;
		double inputDirection = inputImage.getDirection();
		String inputOrigin = inputImage.getOrigin();
		Vector3d inputLocation = inputImage.getLocation();
		if(inputDirection == this.direction && inputOrigin == this.originName && inputLocation.equals(this.location)){
			return true;
		}
		return false;
	}
}
