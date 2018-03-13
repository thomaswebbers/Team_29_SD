package main.java.softdesign;

import javax.vecmath.Vector3d;

public class Message {
	private String data;
	private String originName;
	private Vector3d originLocation;
	private int receiveDate;

	public Message(String data, String originName, Vector3d originLocation, int receiveDate) {
		this.data = data;
		this.originName = originName;
		this.originLocation = originLocation;
		this.receiveDate = receiveDate;
	}

	public String getData() {
		return data;
	}

	public String getOriginName() {
		return originName;
	}

	public Vector3d getOriginLocation() {
		return originLocation;
	}

	public int getReceiveDate() {
		return receiveDate;
	}
}
