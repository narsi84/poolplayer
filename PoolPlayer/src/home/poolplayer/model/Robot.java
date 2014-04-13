package home.poolplayer.model;

import home.poolplayer.controller.Controller;
import home.poolplayer.robot.Move;
import home.poolplayer.robot.PathPlanner;
import home.poolplayer.robot.RemotePilotControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencv.core.Point;

public class Robot {

	private static Logger logger = Logger.getLogger(Controller.LOGGERNAME);

	// In cm
	private static double CUETIP_CUEBALL_DIST = 15;

	private static double CUESTICK_LENGTH = 5; // Length between green and blue
												// circles on stick in cm
	private static float ANGLE_ERROR_CORRECTION = 8.2f / 9.0f;
	private static float LINEAR_ERROR_CORRECTION = 0.925f;

	private static float CUE_MOTOR_CORRECTION = 2.86f;
	
	//private static double RESOLUTION = 0.079;
	private static double RESOLUTION = 0.07;

	private static double[] offset = new double[] { 3, 3 };

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
		if (center == null) {
			this.center = null;
			return;
		}

		this.center = transformPoint(center, getOrientation(), offset);
	}

	private Point transformPoint(Point p, double theta_deg, double[] offset) {
//		double cueStickLengthInPixels = Controller.getInstance().getCueStick().getLength();
//		double resln = CUESTICK_LENGTH / cueStickLengthInPixels;

		double resln = RESOLUTION;

		double alpha_x = offset[0] / resln;
		double alpha_y = offset[1] / resln;
		double theta = theta_deg * Math.PI / 180;
		double x_p = alpha_x * Math.cos(theta) + alpha_y * Math.sin(theta)
				+ p.x;
		double y_p = -alpha_x * Math.sin(theta) + alpha_y * Math.cos(theta)
				+ p.y;

		return new Point(x_p, y_p);
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

		double deg = 90
				- Math.atan2(stick.end.y - stick.start.y, stick.end.x
						- stick.start.x) * 180 / Math.PI;
		 return deg < 0 ? 360 + deg : deg;
	}

	public double getOrientation2() {
		CueStick stick = Controller.getInstance().getCueStick();
		if (stick == null)
			return Double.NaN;

		Point p = stick.getIntersection(center);
		double deg = Math.atan2(p.y - center.y, p.x - center.x) * 180 / Math.PI;
		return deg > 0 ? deg : 360 + deg;
	}

	public boolean initialize() {
		boolean res = pilot.connect(nxtName, null);
		if (res) {
			pilot.setMoveSpeed(10);
			pilot.setTurnSpeed(40);
		}
		return res;
	}

	public void exit() {
		pilot.exit();
	}

	/*
	 * public boolean makeShot(Shot shot, List<Move> moves) { pilot.rotate(180);
	 * return true; }
	 */

	public boolean makeShot(Shot shot, List<Move> moves) {
		if (moves == null || moves.isEmpty()) {
			logger.info("****** Nothing to move ****** ");
			return false;
		}

		// Find initial orientation of bot
		double initialDir = getOrientation();

		// 1px = resln cm

		double resln = RESOLUTION;
		// Execute moves
		for (Move move : moves) {
			logger.debug("Execu ting move " + move.toString());
			float minRotation = (float)findBestRotation(initialDir, move.direction);

			pilot.rotate(minRotation * ANGLE_ERROR_CORRECTION);
			// We need to move in wheel dia units. 1 wheel dia = 3cm. 1px =
			// resnl cm. Therefore, move.dist px = move.dist*resln cm
			pilot.travel((float) (move.dist * resln * LINEAR_ERROR_CORRECTION));

			initialDir = move.direction;
		}

		// Find orientation at dest: angle of ghost-cueball line. This angle is
		// wrt S.
		// So final orientation = 180 - bot orientation + finalDir
		double finalDir = Math.atan2(shot.ghost.x - shot.cueBall.x,
				shot.ghost.y - shot.cueBall.y) * 180 / Math.PI;

		// Rotate to final orientation
		float finalRotation = (float)findBestRotation(moves.get(moves.size() - 1).direction, finalDir+180);
		logger.debug("Aligning to final dir. Rotate by " + finalRotation);
		pilot.rotate(finalRotation * ANGLE_ERROR_CORRECTION);

		// Move cuestick closer to cue ball so that stick is CUETIP_CUEBALL_DIST
		// from cueball.

		// Cue start will now be different from initial.
		// start.x = final bot.center.x- (offset[0] + CUE_STICK_LENGTH).
		// start.y = final bot.center.y - offset[1].
		// Apply affine tx.
		Point finalBotPost = PathPlanner.getGoal(shot);
		double[] start2center = new double[] { -offset[0],
				-(offset[1] + CUESTICK_LENGTH) };
		// finalDir is wrt S, so add 180.
		Point cueStart = transformPoint(finalBotPost, 180+finalDir, start2center);

		double dist = Math.sqrt((cueStart.x - shot.cueBall.x)
				* (cueStart.x - shot.cueBall.x) + (cueStart.y - shot.cueBall.y)
				* (cueStart.y - shot.cueBall.y));

		// We need to move dist - CUETIP_CUEBALL_DIST. So reduce that
		dist = dist * resln - CUETIP_CUEBALL_DIST;

		// Go slow
		pilot.setShootSpeed(30);

		// dist px = dist*resln cm. Motor is in reverse, so negate
		logger.debug("Moving closer to cue ball. Shooting " + dist);

		pilot.shoot(-(float) (dist * CUE_MOTOR_CORRECTION));
		// Make shot
		pilot.setShootSpeed(100);
		// pilot.setShootSpeed((float) shot.velocity);
		// Allow for some error
		logger.debug("Shooting");
		pilot.shoot(-(float) (CUETIP_CUEBALL_DIST * CUE_MOTOR_CORRECTION));

		// Retract cue stick
		pilot.setShootSpeed(100);
		pilot.shoot((float) ((dist + CUETIP_CUEBALL_DIST) * CUE_MOTOR_CORRECTION));

		return true;
	}

	/**
	 * Find whether to rotate clockwise or anticlockwise
	 * @param initDir
	 * @param finalDir
	 * @return
	 */
	private double findBestRotation(double initDir, double finalDir){
		double r = finalDir - initDir;
		double ar = Math.abs(r);
		double rot;
		if (ar < 180)
			rot = r;
		else if (initDir > finalDir)
			rot = 360-ar;
		else
			rot = ar - 360;
		return rot;			
	}
	
	@Override
	public String toString() {
		return center.toString() + ", " + getOrientation() + " N";
	}

	public static void main(String[] args) {
		CueStick stick = new CueStick(new Point(396.0, 103.0), new Point(407.0,
				559.0));
		Controller.getInstance().setCueStick(stick);

		List<Move> moves = new ArrayList<Move>();
		moves.add(new Move(141, 180));
		moves.add(new Move(283, 270));
		moves.add(new Move(303, 180));

		Shot shot = new Shot();
		shot.cueBall = new PoolBall(440.5, 691.5, 10, BallType.CUE);
		shot.ghost = new PoolBall(240.5, 691.5, 10, BallType.CUE);
		shot.stick = stick;

		Robot r = new Robot();
		r.initialize();

		r.setCenter(new Point(357.5, 413.5));

		r.makeShot(shot, moves);
		r.exit();
	}
}
