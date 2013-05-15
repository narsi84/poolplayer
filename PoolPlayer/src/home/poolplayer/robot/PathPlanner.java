package home.poolplayer.robot;

import home.poolplayer.controller.Controller;
import home.poolplayer.model.PoolTable;
import home.poolplayer.model.Shot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class PathPlanner {
	static class Tuple {
		int r, c, g;

		public Tuple(int r_, int c_, int g_) {
			r = r_;
			c = c_;
			g = g_;
		}
	}

	static class TupleComparator implements Comparator<Tuple> {

		@Override
		public int compare(Tuple t1, Tuple t2) {

			if (t1.g < t2.g)
				return -1;
			if (t1.g > t2.g)
				return 1;
			return 0;
		}
	}

	enum Direction {
		N(-1, 0, 1, 0), S(1, 0, 1, 180), E(0, 1, 1, 270), W(0, -1, 1, 90), O(0, 0, 0, 0);
		int r, c, cost, angle;

		private Direction(int r_, int c_, int cost_, int angle_) {
			r = r_;
			c = c_;
			cost = cost_;
			angle = angle_;
		}
	}

	private static Logger logger = Logger.getLogger(Controller.LOGGERNAME);
	
	private static int[][] getGridMap(Mat src) {
		int[][] gridMap = new int[src.height()][src.width()];

		PoolTable table = Controller.getInstance().getTable();
		int t_w = table.getWidth();
		int t_h = table.getHeight();
		int t_x = table.getX();
		int t_y = table.getY();
		int clearance = (int) table.getClearance();

		for (int i = t_y - clearance; i < t_y + t_h + clearance; i++)
			for (int j = t_x - clearance; j < t_x + t_w + clearance; j++)
				gridMap[i][j] = 1;

		return gridMap;
	}

	public static List<Move> getPath(Shot shot, Mat src){			
		logger.info("****** Finding path *******");

		// Find bounding box of table (depends on size of bot)
		int[][] gridMap = getGridMap(src);

		// Get goal position
		Point goal = getGoal(shot);		
		
		Point origin = Controller.getInstance().getRobot().getCenter();

		logger.debug("Origin: " + origin.toString() + " Goal: " + goal.toString());

		// Transpose coordinates to feed to PathPlanner
		Point center_t = new Point((int)origin.y, (int)origin.x);
		Point goal_t = new Point((int)goal.y, (int)goal.x);

		
		// Get path from path planner
		List<Move> path = getShortestPath(gridMap, center_t, goal_t);
		if (path == null || path.size() == 0){
			logger.info("******* No path found to goal ********");
			return null;
		}
		
		return path;
	}

	private static List<Move> getShortestPath(int[][] grid, Point origin, Point goal) {
		List<Move> moves = new ArrayList<Move>();

		//width is no. of columns
		int width = grid[0].length;
		//height is no. of rows
		int height = grid.length;
		
		int[][] visited = new int[height][width];
		Direction[][] policy = new Direction[height][width];
		Direction[][] action = new Direction[height][width];
		List<Direction> path = new ArrayList<PathPlanner.Direction>();

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				visited[i][j] = 0;
				action[i][j] = Direction.O;
				policy[i][j] = Direction.O;
			}
		}

		List<Tuple> opened = new ArrayList<PathPlanner.Tuple>();
		TupleComparator comparator = new TupleComparator();

		int r = (int) origin.x, c = (int) origin.y;
		int g = 0;

		visited[r][c] = 1;

		opened.add(new Tuple(r, c, g));

		boolean found = false, resign = false;

		while (!found && !resign) {
			if (opened.size() == 0) {
				resign = true;
				break;
			}

			Collections.sort(opened, comparator);
			Tuple minTuple = opened.remove(0);		

			if (minTuple.r == goal.x && minTuple.c == goal.y) {
				found = true;
				break;
			}

			for (Direction dir : Direction.values()) {
				if (dir == Direction.O)
					continue;
				
				int r2 = minTuple.r + dir.r;
				int c2 = minTuple.c + dir.c;
				int g2 = minTuple.g + dir.cost;

				if (!(r2 >= 0 && c2 >= 0 && r2 < height && c2 < width))
					continue;

				if (visited[r2][c2] == 1 || grid[r2][c2] != 0)
					continue;

				Tuple newTuple = new Tuple(r2, c2, g2);
				opened.add(newTuple);

				visited[r2][c2] = 1;
				action[r2][c2] = dir;
			}
		}

		if (!found){
			return moves;
		}
		
		// Calculating policy
		int rg = (int) goal.x, cg = (int) goal.y;

		
		while (rg!=r || cg != c) {
			int r2 = rg - action[rg][cg].r;
			int c2 = cg - action[rg][cg].c;
			path.add(action[rg][cg]);
			rg = r2;
			cg = c2;
		}
		
		if (path.isEmpty()){
			logger.info("****** No path found *******");
			return moves;
		}
		
	 	Direction previous = path.get(path.size() - 1);	 	
		int ctr = 1;
		Move move = new Move();
		move.direction = previous.angle;
		move.dist = ctr;
		moves.add(move);
			
		for (int i = path.size() - 2; i >= 0; i--) {
			Direction current = path.get(i);
			if (previous != current) {
				
				ctr = 1;
				move = new Move();
				move.direction = current.angle;
				move.dist = ctr;
				moves.add(move);
			} else {
				ctr++;
				move.dist = ctr;
			}
			previous = current;
		}

		logger.info("****** Found path *******");
		for(Move m : moves){
			logger.debug(m.dist + " : " + m.direction);
		}
		
		return moves;
	}
	
	// Find all sides where the above line intersects the bounding box. This
	// is where the bot can be. Of all the above points, find the one where
	// the dist from bot-cue < bot-ghost
	private static Point getGoal(Shot shot) {
		double y1 = shot.cueBall.getY(), x1 = shot.cueBall.getX();
		double y2 = shot.ghost.getY(), x2 = shot.ghost.getX();

		double m, c;
		if (x2 == x1) {
			c = x2;
			m = Double.POSITIVE_INFINITY;
		} else {
			m = (y2 - y1) / (x2 - x1);
			c = (y1 * x2 - y2 * x1) / (x2 - x1);
		}
		
		PoolTable table = Controller.getInstance().getTable();
		int t_w = table.getWidth();
		int t_h = table.getHeight();
		int t_x = table.getX();
		int t_y = table.getY();
		int clearance = (int) table.getClearance();

		double cp;
		
		double x, y, d_cue, d_ghost;
		double minDist = Double.MAX_VALUE;

		Point goal = new Point();
		
		// Left wall
		cp = t_x - clearance;
		x = cp;
		y = m*x + c;
		d_cue = getDist(x, y, shot.cueBall.getX(), shot.cueBall.getY());
		d_ghost = getDist(x, y, shot.ghost.getX(), shot.ghost.getY());
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}
		
		// Right wall
		cp = t_x + t_w + clearance;
		x = cp;
		y = m*x + c;
		d_cue = getDist(x, y, shot.cueBall.getX(), shot.cueBall.getY());
		d_ghost = getDist(x, y, shot.ghost.getX(), shot.ghost.getY());
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}
		
		// Top wall
		cp = t_y - clearance;
		y = cp;
		if (m == 0)
			x = Double.POSITIVE_INFINITY;
		else
			x = (y - c)/m;
		d_cue = getDist(x, y, shot.cueBall.getX(), shot.cueBall.getY());
		d_ghost = getDist(x, y, shot.ghost.getX(), shot.ghost.getY());
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}		
		// Bottom wall
		cp = t_y + t_h + clearance;
		y = cp;
		if (m == 0)
			x = Double.POSITIVE_INFINITY;
		else
			x = (y - c)/m;
		d_cue = getDist(x, y, shot.cueBall.getX(), shot.cueBall.getY());
		d_ghost = getDist(x, y, shot.ghost.getX(), shot.ghost.getY());
		if (d_cue < d_ghost && d_cue < minDist) {
			minDist = d_cue;
			goal.x = x;
			goal.y = y;
		}
		
		return goal;
	}

	private static double getDist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
	
	public static void main(String[] args) {
		int[][] grid = new int[1280][720];
		for(int r=0; r<grid.length; r++){
			for(int c=0; c<grid[0].length; c++){
				grid[r][c] = 0;
			}
		}

		// Mask out center 400x600 
		for(int r=575-20; r<850+20; r++){
			for(int c=160-20; c<620+20; c++){
				grid[r][c] = 1;
			}
		}

		Point origin = new Point(413, 357);
		Point goal = new Point(857, 640);

		List<Move> moves = getShortestPath(grid, origin, goal);
		for(Move m : moves){
			System.out.println(m.dist + " : " + m.direction);
		}						
	}
}
