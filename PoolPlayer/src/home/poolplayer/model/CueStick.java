package home.poolplayer.model;

import org.opencv.core.Point;

public class CueStick {

	public Point start;
	public Point end;	
	
	public CueStick() {
		start = new Point(0, 0);
		end = new Point(0, 0);
	}
}
