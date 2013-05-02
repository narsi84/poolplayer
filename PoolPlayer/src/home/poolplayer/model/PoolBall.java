package home.poolplayer.model;

public class PoolBall extends PoolCircle {
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
	
	public BallType getType() {
		return type;
	}

	public void setType(BallType type) {
		this.type = type;
	}
}