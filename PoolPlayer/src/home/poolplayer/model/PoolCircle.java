package home.poolplayer.model;

public class PoolCircle {
	double x;
	double y;
	double r;

	public PoolCircle() {
	}
	
	public PoolCircle(double x_, double y_, double r_){
		this.x = x_;
		this.y = y_;
		this.r = r_;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getR() {
		return r;
	}

	public void setR(double r) {
		this.r = r;
	}

	public boolean isPointWithin(double x0, double y0) {
		if ((x0 - x) * (x0 - x) + (y0 - y) * (y0 - y) <= r * r)
			return true;
		return false;
	}
	
	
}
