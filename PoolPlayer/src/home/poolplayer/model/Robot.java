package home.poolplayer.model;

import home.poolplayer.controller.Controller;
import home.poolplayer.robot.RemotePilotControl;

import org.opencv.core.Point;

public class Robot {

	private Point center;
	private String nxtName;
    private RemotePilotControl pilot;
	
	public Robot(){
		center = new Point();
		pilot = new RemotePilotControl();
	}	
	
	public Robot(String nxtName_){
		center = new Point();
		nxtName = nxtName_;
	}	

	public Point getCenter() {
		return center;
	}
	
	public void setCenter(Point center) {
		this.center = center;
	}
	
	public String getNxtName() {
		return nxtName;
	}
	
	public void setNxtName(String nxtName) {
		this.nxtName = nxtName;
	}
	
	public double getOrientation() {
		CueStick stick = Controller.getInstance().getCueStick();
		if (stick == null)
			return Double.NaN;
		
		Point p = stick.getIntersection(center);
		double deg = Math.atan2(p.y - center.y, p.x - center.x) * 180 / Math.PI;
		return deg > 0 ? deg : 360 + deg;
	}
	
	public boolean initialize(){
		return pilot.connect(nxtName, null);		
	}
}
