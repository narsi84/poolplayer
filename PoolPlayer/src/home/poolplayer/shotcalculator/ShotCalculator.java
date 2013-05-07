package home.poolplayer.shotcalculator;

import home.poolplayer.controller.Controller;
import home.poolplayer.model.BallType;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolCircle;

public class ShotCalculator {

	private static PoolBall findGhost(PoolBall ball, PoolCircle pocket) {
		double theta = Math.atan2(pocket.getY() - ball.getY(), pocket.getX()
				- ball.getX());
		PoolBall ghost = new PoolBall(ball.getX() - 2 * PoolBall.AVG_SIZE
				* Math.cos(theta), ball.getY() - 2 * PoolBall.AVG_SIZE
				* Math.sin(theta), ball.getR(), BallType.GHOST);
		return ghost;
	}

	private static boolean isBallInPath(PoolCircle src_t, PoolCircle dest_t,
			PoolCircle ball) {
		if (norm(src_t, ball) < 2 * PoolBall.AVG_SIZE
				|| norm(dest_t, ball) < 2 * PoolBall.AVG_SIZE)
			return true;

		double error = 2;

		PoolCircle src = new PoolCircle(src_t.getX() - ball.getX(),
				src_t.getY() - ball.getY(), src_t.getR());
		PoolCircle dest = new PoolCircle(dest_t.getX() - ball.getX(),
				dest_t.getY() - ball.getY(), dest_t.getR());

		double theta = Math.PI
				/ 2
				- Math.atan2(dest.getY() - src.getY(), dest.getX() - src.getX());

		// Center
		double dr = norm(dest, src);
		double D = src.getX() * dest.getY() - dest.getX() * src.getY();

		// Right line
		PoolCircle src_r = new PoolCircle(src.getX() + PoolBall.AVG_SIZE
				* Math.cos(theta), src.getY() - PoolBall.AVG_SIZE
				* Math.sin(theta), src.getR());
		PoolCircle dest_r = new PoolCircle(dest.getX() + PoolBall.AVG_SIZE
				* Math.cos(theta), dest.getY() - PoolBall.AVG_SIZE
				* Math.sin(theta), dest.getR());
		double D_r = src_r.getX() * dest_r.getY() - dest_r.getX()
				* src_r.getY();

		// Left line
		PoolCircle src_l = new PoolCircle(src.getX() - PoolBall.AVG_SIZE
				* Math.cos(theta), src.getY() + PoolBall.AVG_SIZE
				* Math.sin(theta), src.getR());
		PoolCircle dest_l = new PoolCircle(dest.getX() - PoolBall.AVG_SIZE
				* Math.cos(theta), dest.getY() + PoolBall.AVG_SIZE
				* Math.sin(theta), dest.getR());
		double D_l = src_l.getX() * dest_l.getY() - dest_l.getX()
				* src_l.getY();

		double delta_c = PoolBall.AVG_SIZE * PoolBall.AVG_SIZE * dr * dr - D
				* D;
		double delta_r = PoolBall.AVG_SIZE * PoolBall.AVG_SIZE * dr * dr - D_r
				* D_r;
		double delta_l = PoolBall.AVG_SIZE * PoolBall.AVG_SIZE * dr * dr - D_l
				* D_l;

		// Ball cannot be in path if src-dest dist > src-ball. Account for
		// radius of ball and source
		if (norm(src_t, dest_t) + 2 * PoolBall.AVG_SIZE - error < norm(src_t,
				ball))
			return false;

		if (delta_c > 0 || delta_l > 0 || delta_r > 0) {
			// If both dest and src are on the same size of ball, there is no
			// overlap. The angle subtended at ball by src-ball and dest-ball
			// will be acute.
			if (angleAt(ball, src_t, dest_t) < Math.PI / 2)
				return false;
			else
				return true;
		} else
			return false;
	}

	// The angle subtended at ghost by cue-ghost and ghost-pocket vectors must
	// be acute
	private static boolean isShotPossible(PoolBall ghost, PoolBall cue,
			PoolCircle pocket) {
		return angleAt(ghost, cue, pocket) < Math.PI / 2 ? true : false;
	}

	// Find initial velocity of cue to pot target into pocket
	private static double fintInitalVelocity(PoolBall cue, PoolBall ghost,
			PoolBall target, PoolCircle pocket) {
		double accel = Controller.getInstance().getTable().getFriction();

		double theta_gc = Math.atan2(ghost.getY() - cue.getY(), ghost.getX()
				- cue.getX());
		double theta_gt = Math.atan2(target.getY() - ghost.getY(),
				target.getX() - ghost.getX());
		double theta_ct = theta_gc - theta_gt;
		double s = norm(target, pocket);
		double d = norm(cue, ghost);

		double u_c = Math.sqrt(2 * accel * s
				/ (Math.cos(theta_ct) * Math.cos(theta_ct)) + 2 * accel * d);
		return u_c;

	}

	private static double angleAt(PoolCircle c, PoolCircle byBall1,
			PoolCircle byBall2) {
		double cb1 = norm(c, byBall1);
		double b1b2 = norm(byBall1, byBall2);
		double cb2 = norm(c, byBall2);

		// Law of cosines
		double theta = Math.acos((cb1 * cb1 + cb2 * cb2 - b1b2 * b1b2)
				/ (2 * cb1 * cb2));
		return theta;
	}

	private static double norm(PoolCircle c1, PoolCircle c2) {
		return Math.sqrt((c1.getX() - c2.getX()) * (c1.getX() - c2.getX())
				+ (c1.getY() - c2.getY()) * (c1.getY() - c2.getY()));
	}
}
