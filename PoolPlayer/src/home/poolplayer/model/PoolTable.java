package home.poolplayer.model;

import java.util.ArrayList;
import java.util.List;

public class PoolTable {

	public static int NUM_POCKETS = 6;
	
	private int height;
	private int width;
	
	private List<PoolCircle> pockets;
	
	private double friction;

	private static PoolTable instance;
	
	private PoolTable(){		
		pockets = new ArrayList<PoolCircle>();
		
		width = 600;
		height = 300;
		friction = 5;
		
		initPocketPositions();
	}
	
	private void initPocketPositions(){
		
	}
	
	public static PoolTable getInstance(){
		if (instance == null)
			instance = new PoolTable();
		return instance;
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
}
