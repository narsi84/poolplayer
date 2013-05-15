package home.poolplayer.model;

public class Shot {

	public PoolBall cueBall;
	public PoolBall ballOn;
	public PoolBall ghost;
	public PoolCircle pocket;
	public CueStick stick;
	public double velocity;	
	
	@Override
	public String toString() {
		String str = "\nCue ball: " + cueBall.toString() + "\n";
		str += "Ball on: " + ballOn.toString() + "\n";
		str += "Ghost : " + ghost.toString() + "\n";
		str += "Pocket: " + pocket.toString() + "\n";
		str += "Velocity:" + velocity;
		return str;
	}
}
