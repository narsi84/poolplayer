package home.poolplayer.model;

import home.poolplayer.controller.Controller;
import home.poolplayer.robot.RemotePilotControl;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Robot {

	private Point center;
	private String nxtName;
	private RemotePilotControl pilot;

	public Robot() {
		center = new Point();
		pilot = new RemotePilotControl();
	}

	public Robot(String nxtName_) {
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

	public boolean initialize() {
		return pilot.connect(nxtName, null);
	}

	public void exit() {
		pilot.exit();
	}

	private int[][] getGridMap(Mat src) {
		int[][] gridMap = new int[src.width()][src.height()];

		PoolTable table = Controller.getInstance().getTable();
		int t_w = table.getWidth();
		int t_h = table.getHeight();
		int t_x = table.getX();
		int t_y = table.getY();
		int clearance = (int) table.getClearance();

		for (int i = t_x - clearance; i < t_w + clearance; i++)
			for (int j = t_y - clearance; j < t_h + clearance; j++)
				gridMap[i][j] = 1;

		return gridMap;
	}

	public boolean makeShot(Shot shot, Mat src) {
		// Find initial orientation of bot
		double theta_0 = getOrientation();

		// Find bounding box of table (depends on size of bot)
		int[][] gridMap = getGridMap(src);

		// Find orientation at dest: angle of ghost-cueball line
		double theta_1 = Math.atan2(shot.ghost.y - shot.cueBall.y, shot.ghost.x
				- shot.cueBall.x)
				* 180 / Math.PI;

		// Get goal position
		Point goal = getGoal(shot);

		// Get path from path planner
		
		
		// Orient bot at initial post
		// Move bot
		// Orient bot at final post
		// Move cuestick closer to cue ball
		// Make shot
		// Retract cue stick

		return false;
	}

	// Find all sides where the above line intersects the bounding box. This
	// is where the bot can be. Of all the above points, find the one where
	// the dist from bot-cue < bot-ghost
	private Point getGoal(Shot shot) {
		double y1 = shot.cueBall.y, x1 = shot.cueBall.x;
		double y2 = shot.ghost.y, x2 = shot.ghost.x;

		double m, c;
		if (x2 == x1) {
			c = x2;
			m = Double.POSITIVE_INFINITY;
		} else {
			m = (y2 - y1) / (x2 - x1);
			c = (y1 * x2 - y2 * x1) / (x2 - x1);
		}
		
		PoolTable table = Controller.getInstance().getTable();
		int t_w = table.getWidth();
		int t_h = table.getHeight();
		int t_x = table.getX();
		int t_y = table.getY();
		int clearance = (int) table.getClearance();

		double cp;
		
		double x, y, d_cue, d_ghost;
		double minDist = Double.MAX_VALUE;

		Point goal = new Point();
		
		// Left wall
		cp = t_x - clearance;
		x = cp;
		y = m*x + c;
		d_cue = getDist(x, y, shot.cueBall.x, shot.cueBall.y);
		d_ghost = getDist(x, y, shot.ghost.x, shot.ghost.y);
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}
		
		// Right wall
		cp = t_x + t_w + clearance;
		x = cp;
		y = m*x + c;
		d_cue = getDist(x, y, shot.cueBall.x, shot.cueBall.y);
		d_ghost = getDist(x, y, shot.ghost.x, shot.ghost.y);
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}
		
		// Top wall
		cp = t_y - clearance;
		y = cp;
		if (m == 0)
			x = Double.POSITIVE_INFINITY;
		else
			x = (y - c)/m;
		d_cue = getDist(x, y, shot.cueBall.x, shot.cueBall.y);
		d_ghost = getDist(x, y, shot.ghost.x, shot.ghost.y);
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}		
		// Bottom wall
		cp = t_y + t_h + clearance;
		y = cp;
		if (m == 0)
			x = Double.POSITIVE_INFINITY;
		else
			x = (y - c)/m;
		d_cue = getDist(x, y, shot.cueBall.x, shot.cueBall.y);
		d_ghost = getDist(x, y, shot.ghost.x, shot.ghost.y);
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}
		
		return goal;
	}

	private double getDist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
}
