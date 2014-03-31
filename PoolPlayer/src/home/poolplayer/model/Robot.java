package home.poolplayer.model;

import home.poolplayer.controller.Controller;
import home.poolplayer.robot.Move;
import home.poolplayer.robot.RemotePilotControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencv.core.Point;

public class Robot {

	private static Logger logger = Logger.getLogger(Controller.LOGGERNAME);

	// In cm
	private static double CUETIP_BOTCENTER_DIST = 16.5;
	private static double CUETIP_CUEBALL_DIST = 5;

	private Point center;
	private String nxtName;
	private RemotePilotControl pilot;

	public Robot() {
		center = new Point();
//		pilot = new RemotePilotControl();
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

	public boolean makeShot(Shot shot, List<Move> moves) {
		if (moves == null || moves.isEmpty()) {
			logger.info("****** Nothing to move ****** ");
			return false;
		}

		// Find initial orientation of bot
		double initialDir = getOrientation();

		// Get the min dist from center to either end of the stick. This is should be 16.5cm 
		double centerToStickTipStart = Math.sqrt((center.x - shot.stick.start.x)
				* (center.x - shot.stick.start.x)
				+ (center.y - shot.stick.start.y)
				* (center.y - shot.stick.start.y));

		double centerToStickTipEnd = Math.sqrt((center.x - shot.stick.end.x)
				* (center.x - shot.stick.end.x)
				+ (center.y - shot.stick.end.y)
				* (center.y - shot.stick.end.y));

		double centerToStickTip = Math.min(centerToStickTipStart, centerToStickTipEnd);
		// 1px = resln cm
		double resln = 16.5 / centerToStickTip;

		// Execute moves
		for (Move move : moves) {
			pilot.rotate((float) (move.direction - initialDir));

			// We need to move in wheel dia units. 1 wheel dia = 3cm. 1px =
			// resnl cm. Therefore, move.dist px = move.dist*resln cm
			pilot.travel((float) (move.dist * resln));

			initialDir = move.direction;
		}

		// Find orientation at dest: angle of ghost-cueball line
		double finalDir = Math.atan2(shot.ghost.y - shot.cueBall.y,
				shot.ghost.x - shot.cueBall.x) * 180 / Math.PI;

		// Rotate to final orientation
		pilot.rotate((float) (finalDir - moves.get(moves.size() - 1).direction));

		// Move cuestick closer to cue ball so that stick is CUETIP_CUEBALL_DIST
		// from cueball
		double dist = Math.sqrt((center.x - shot.cueBall.x)
				* (center.x - shot.cueBall.x) + (center.y - shot.cueBall.y)
				* (center.y - shot.cueBall.y));

		// We need to move dist - CUETIP_CUEBALL_DIST, but cuestick is already
		// CUETIP_BOTCENTER_DIST from bot center. So reduce that
		dist -= CUETIP_BOTCENTER_DIST;
		dist -= CUETIP_CUEBALL_DIST;

		// Go slow
		pilot.setShootSpeed(30);

		// dist px = dist*resln cm. Motor is in reverse, so negate
		pilot.shoot(-(float) (dist * resln));

		// Make shot
		pilot.setShootSpeed((float) shot.velocity);
		// Allow for some error
		pilot.shoot(-(float) (CUETIP_CUEBALL_DIST + 1));

		// Retract cue stick
		pilot.setShootSpeed(100);
		pilot.shoot((float) (dist*resln + CUETIP_CUEBALL_DIST + 1));

		return true;
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
