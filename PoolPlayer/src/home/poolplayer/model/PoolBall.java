package home.poolplayer.model;

public class PoolBall extends PoolCircle {
	
	public static double AVG_SIZE;
	
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
	
	public PoolBall(double x_, double y_, double r_, BallType type_){
		this.x = x_;
		this.y = y_;
		this.r = r_;
		this.type = type_;
	}
	
	public BallType getType() {
		return type;
	}

	public void setType(BallType type) {
		this.type = type;
	}
}