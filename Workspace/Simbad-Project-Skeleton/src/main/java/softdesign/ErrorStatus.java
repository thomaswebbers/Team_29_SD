package main.java.softdesign;

public enum ErrorStatus {
	IRError , RadioError, LocomotionError, SensorError, CameraError, NoErrors
	//IRError: short range communication offline, can no longer identify local agents (problem when colliding) (IR = infrared)
	//RadioError: long range communication offline, can no longer communicate with ControlCenter
	//LocomotionError: Locomotion offline, can no longer move
	//SensorError: Sensors offline, can no longer identify obstacles
	//CameraError: Camera offline, can no longer take pictures
}
