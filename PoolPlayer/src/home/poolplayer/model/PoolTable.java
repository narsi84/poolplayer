package home.poolplayer.model;

import java.util.ArrayList;
import java.util.List;

public class PoolTable {

	public static int NUM_POCKETS = 6;

	private int height;
	private int width;
	private int x;
	private int y;

	private List<PoolCircle> pockets;
	private int pocketRadius;

	private double friction;
	private double clearance; 

	public PoolTable() {
		pockets = new ArrayList<PoolCircle>();
	}

	public void initPocketPositions() {
		pockets.clear();

		PoolCircle tl = new PoolCircle(x, y, pocketRadius);
		PoolCircle tc = new PoolCircle(x + width / 2, y, pocketRadius);
		PoolCircle tr = new PoolCircle(x + width, y, pocketRadius);
		PoolCircle bl = new PoolCircle(x, y + height, pocketRadius);
		PoolCircle bc = new PoolCircle(x + width / 2, y + height, pocketRadius);
		PoolCircle br = new PoolCircle(x + width, y + height, pocketRadius);

		pockets.add(tl);
		pockets.add(tc);
		pockets.add(tr);
		pockets.add(bl);
		pockets.add(bc);
		pockets.add(br);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public List<PoolCircle> getPockets() {
		return pockets;
	}

	public void setPockets(List<PoolCircle> pockets) {
		this.pockets = pockets;
	}

	public double getFriction() {
		return friction;
	}

	public void setFriction(double friction) {
		this.friction = friction;
	}

	public int getPocketRadius() {
		return pocketRadius;
	}

	public void setPocketRadius(int pocketRadius) {
		this.pocketRadius = pocketRadius;
	}
	
	public double getClearance() {
		return clearance;
	}
	
	public void setClearance(double clearance) {
		this.clearance = clearance;
	}	
}
