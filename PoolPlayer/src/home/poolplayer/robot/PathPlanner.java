package home.poolplayer.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		N(-1, 0, 1, 0), S(1, 0, 1, -180), E(0, 1, 1, 270), W(0, -1, 1, 90), O(0, 0, 0, 0);
		int r, c, cost, angle;

		private Direction(int r_, int c_, int cost_, int angle_) {
			r = r_;
			c = c_;
			cost = cost_;
			angle = angle_;
		}
	}

	public static List<Move> getPath(int[][] grid, Point origin, Point goal) {
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
			System.out.println("Goal unreachable");
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
		
		if (path.isEmpty())
			return moves;
		
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
		
		return moves;
	}
	
	public static void main(String[] args) {
		int[][] grid = new int[600][800];
		for(int c=0; c<grid.length; c++){
			for(int r=0; r<grid[0].length; r++){
				grid[c][r] = 0;
			}
		}

		// Mask out center 400x600 
		for(int c=100; c<400; c++){
			for(int r=100; r<600; r++){
				grid[c][r] = 1;
			}
		}

		Point origin = new Point(550, 200);
		Point goal = new Point(550, 200);

		List<Move> moves = getPath(grid, origin, goal);
		for(Move m : moves){
			System.out.println(m.dist + " : " + m.direction);
		}						
	}
}
