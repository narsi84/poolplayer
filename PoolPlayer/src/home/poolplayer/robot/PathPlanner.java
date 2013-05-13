package home.poolplayer.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Point;

public class PathPlanner {
	static class Tuple {
		int x, y, g;

		public Tuple(int x_, int y_, int g_) {
			x = x_;
			y = y_;
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
		N(0, -1, 1), S(0, 1, 1), E(1, 0, 1), W(-1, 0, 1), O(0, 0, 1);

		int x, y, cost;

		private Direction(int x_, int y_, int cost_) {
			x = x_;
			y = y_;
			cost = cost_;
		}
	}

	public static List<Move> getPath(int[][] grid, Point origin, Point goal) {
		List<Move> moves = new ArrayList<Move>();

		int width = grid.length;
		int height = grid[0].length;

		int[][] visited = new int[width][height];
		Direction[][] policy = new Direction[width][height];
		Direction[][] action = new Direction[width][height];
		List<Direction> path = new ArrayList<PathPlanner.Direction>();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				System.out.println("x:" + i + " y:" + j + " " + grid[i][j]);
				visited[i][j] = 0;
				action[i][j] = Direction.O;
				policy[i][j] = Direction.O;
			}
		}

		List<Tuple> opened = new ArrayList<PathPlanner.Tuple>();
		TupleComparator comparator = new TupleComparator();

		int x = (int) origin.x, y = (int) origin.y;
		int g = 0;

		visited[x][y] = 1;

		opened.add(new Tuple(x, y, g));

		boolean found = false, resign = false;

		while (!found && !resign) {
			if (opened.size() == 0) {
				resign = true;
				break;
			}

			Collections.sort(opened, comparator);
			Tuple minTuple = opened.remove(0);		

			if (minTuple.x == goal.x && minTuple.y == goal.y) {
				found = true;
				break;
			}

			for (Direction dir : Direction.values()) {
				if (dir == Direction.O)
					continue;
				
				int x2 = minTuple.x + dir.x;
				int y2 = minTuple.y + dir.y;
				int g2 = minTuple.g + dir.cost;

				if (!(x2 >= 0 && y2 >= 0 && x2 < width && y2 < height))
					continue;

				if (visited[x2][y2] == 1 || grid[x2][y2] == 1)
					continue;

				Tuple newTuple = new Tuple(x2, y2, g2);
				opened.add(newTuple);

				visited[x2][y2] = 1;
				action[x2][y2] = dir;
				System.out.println("Action: " + y2 + ", " + x2 + ", " + g2 + ", " +dir.name());
			}
		}

		if (!found)
			return moves;
		// Calculating policy
		int xg = (int) goal.x, yg = (int) goal.y;
		while (xg != x && yg != y) {
			int x2 = xg - action[xg][yg].x;
			int y2 = yg - action[xg][yg].y;

			path.add(action[xg][yg]);
			xg = x2;
			yg = y2;
		}

		Direction previous = path.get(path.size() - 1);
		int ctr = 1;
		for (int i = path.size() - 2; i >= 0; i--) {
			Direction current = path.get(i);
			if (previous != current) {
				ctr = 1;
				Move move = new Move();
				switch (previous) {
				case N:
					move.direction = 0;
					move.dist = ctr;
					break;
				case S:
					move.direction = 180;
					move.dist = ctr;
					break;
				case E:
					move.direction = 270;
					move.dist = 0;
					break;
				case W:
					move.direction = 90;
					move.dist = 0;
				default:
					move.direction = 0;
					move.dist = 0;
				}

				moves.add(move);
				
			} else {
				ctr++;
			}
			previous = current;
		}

		return moves;
	}
	
	public static void main(String[] args) {
		int[][] grid = new int[][] {
				{0, 0, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0},
				{0, 1, 1, 1, 0, 0},
				{0, 0, 0, 1, 0, 0},
				{0, 0, 0, 0, 0, 0}				
		};
		
		Point origin = new Point(0, 0);
		Point goal = new Point(2, 0);
		
		List<Move> moves = getPath(grid, origin, goal);
		for(Move m : moves){
			System.out.println(m.dist + " : " + m.direction);
		}		
	}
}