package home.poolplayer.model;

public class PoolBall {
	private double x;
	private double y;
	private double r;
	private BallType type = BallType.NONE;
	
	public PoolBall() {
	}

	public PoolBall(PoolBall old){
		this.x = old.x;
		this.y = old.y;
		this.r = old.r;
		this.type = old.type;
	}
	
	public PoolBall(double[] ball){
		this.x = ball[0];
		this.y = ball[1];
		this.r = ball[2];
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

	public BallType getType() {
		return type;
	}

	public void setType(BallType type) {
		this.type = type;
	}
}