package home.poolplayer.model;

import org.opencv.core.Point;

public class CueStick {

	public Point start;
	public Point end;

	public CueStick() {
		start = new Point(0, 0);
		end = new Point(0, 0);
	}

	public CueStick(Point start_, Point end_) {
		start = new Point(start_.x, start_.y);
		end = new Point(end_.x, end_.y);
	}

	public double getSlope() {
		return (end.y - start.y) / (end.x - start.x);
	}

	public double getIntercept() {
		return (start.y * end.x - end.y * start.x) / (end.x - start.x);
	}

	/**
	 * Returns the point at which the perpendicular from a point <i>p</i> will
	 * intersect this line
	 * 
	 * @param p
	 * @return
	 */
	public Point getIntersection(Point p) {
		double C = getIntercept();
		double m = getSlope();
		double y = p.x * C / (p.x + m * p.y);
		double x = -p.y * C / (p.x + m * p.y);
		return new Point(y, x);
	}

	public double getDistanceToPoint(Point p0) {
		Point p1 = getIntersection(p0);
		return Math.sqrt((p1.y - p0.y) * (p1.y - p0.y) + (p1.x - p0.x)
				* (p1.x - p0.x));
	}
}