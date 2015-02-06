
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;

public class ConwayGrid {
	static final boolean CHECK_CELL_COUNT = false;
	static final boolean USE_HASH_ALGORITHM = true;
	Map<Point, Integer> _map;// = new ConcurrentHashMap<Point, Integer>(16, .75f, _game._options.targetThreadCount);
	Set<Integer> localBirthRules, localSurviveRules;
	ConwayGame _game;
	//Number of generations that have passed since the start. Should be reset anytime reset() is called.
	long generations;
	//Maximum number of neighbors that can result in a "survival" condition; used for performances purposes.
	int maxCount;
	//long liveCells;
	//coloredCells[color.value] == number of live cells with that color
	boolean gridChanged;
	Map<Integer, Long> liveCells;
	ConwayHash _hasher;

	public void step() {
		localSurviveRules = new HashSet<Integer>();
		localBirthRules = new HashSet<Integer>();
		localSurviveRules.addAll(_game._options.surviveList);
		localBirthRules.addAll(_game._options.birthList);
		
		maxCount = 0;
		for(int i : localBirthRules) maxCount = Math.max(maxCount, i);
		for(int i : localSurviveRules) maxCount = Math.max(maxCount, i);
		
		
		
		liveCells = new HashMap<Integer, Long>();
		liveCells.put(null, 0l);
		//temporary set to hold relevant cells
		Set<Point> keys = new HashSet<Point>();
		//add every live cells' neighbors to the set
		for(Point p : _map.keySet()) {
			if(_map.get(p) < 0) {
				keys.add(p);
				continue;
			}
			//iterate through the live cell's neighbors
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					keys.add(new Point(p.x + i, p.y + j));
				}
			}
		}

		//iterate through every cell in the set, deciding its fate

		//newGrid = replacement map to place all new live cells
		int threadCount = Math.max(_game._options.targetThreadCount - 1, 1);
		//System.err.printf("Using %d quantity of threads.\n", threadCount);
		Map<Point, Integer> newGrid = new ConcurrentHashMap<Point, Integer>(_map.keySet().size() * 2, .75f, threadCount);

		ExecutorService exe = Executors.newFixedThreadPool(threadCount);
		
		//total = number of current live cells
		int total = keys.size();
		int interval = total / threadCount;
		List<Point> points = new ArrayList<Point>();
		points.addAll(keys);

		//create n threads that iterate over current live cells, determining if they live or not
		//each thread iterates over its own (total/n) portion of live cells
		for (int i = 0; i < threadCount; i++) {
			//create a list containing a (total/n) sized portion of the live cells
			//each thread gets a different portion
			if(i != threadCount - 1) {
				exe.submit(new RunnableEvaluator(points.subList(i*interval, (i+1)*interval), newGrid));
			} else {
				exe.submit(new RunnableEvaluator(points.subList(i*interval, total), newGrid));
			}
		}
		exe.shutdown();
		while(!exe.isTerminated()) Thread.yield();


		//replace old hash_map with new one
		_map = newGrid;
		++generations;
		gridChanged = true;
	}

	public void evalPoint (Point p, Map<Point, Integer> newGrid) {
		//neighborInfo returns a point whose x is number of neighbors and y is majority color (black is returned if tie or black is the only color)
		Point neighborInfo = neighborInfo(p, maxCount);
		int neighbors = neighborInfo.x;
		int majorityColor = neighborInfo.y;
		boolean exists = _map.containsKey(p);
		int color = 0;
		if(exists) color = _map.get(p);
		boolean negative = color < 0;
		//System.err.printf("Negative: " + negative);


		if (exists && !negative) {
			//System.out.printf("live cell thats nonnegative, neighbors = %d\n", neighbors);
			//live cell with nonnegative color
			if (localSurviveRules.contains(neighbors)) {
				//live on, put it in the new _map with its old color
				newGrid.put(p, color);
				/*if(CHECK_CELL_COUNT)
				synchronized(liveCells) {
					if(liveCells.containsKey(color)) liveCells.put(color, liveCells.get(color) + 1l);
					else liveCells.put(color, 1l);
					liveCells.put(null, liveCells.get(null) + 1l);
				}*/
			} else {
				//die
			}
		} else if(!exists || negative) {
			//live cell with negative color, treat it like its dead
			//if it's supposed to be birthed, change its color, otherwise keep its color
			if (localBirthRules.contains(neighbors)) {
				//put it in the new _map with the majority color of its neighbors
				newGrid.put(p, majorityColor);
				/*if(CHECK_CELL_COUNT)
				synchronized(liveCells) {
					if(liveCells.containsKey(majorityColor)) liveCells.put(majorityColor, liveCells.get(majorityColor) + 1l);
					else liveCells.put(majorityColor, 1l);
					liveCells.put(null, liveCells.get(null) + 1l);
				}*/
			} else if(negative) {
				//put it in the new map with its old color
				newGrid.put(p, color);
				//System.err.printf(" and we added it to the new grid!\n");
			}
		}
	}

	public Point neighborInfo(Point p, int cutoff) {
		//colors[color.value] == number of neighbors with that color
		int colors[] = new int[9];
		//for (int i = 0; i < 9; i++) {
			//colors[i] = 0;
		//}
		//n == number of live neighbor cells
		int n = 0;
		boolean hasColor = false;

		//look at all 8 neighbors
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				Point newp = new Point(p.x + i, p.y + j);
				if (_map.containsKey(newp)) {

					//color = color of that neighbor
					int color = _map.get(newp);
					
					if (color >= 0) { //ignore the negative colors (they don't count as neighbors)
						//increment number of live neighbors
						n++;
						if(color > 0) {
							hasColor = true;
							colors[color]++;
						}

						//increment the number of neighbors with that color
						//if (color > 0) {
						//}
					}
					if(n > cutoff) return new Point(n,0);
				}
			}
		}
		if(!hasColor) return new Point(n,0);
		int index = 0;
		int max = 0;
		//boolean isTie = true;
		for(int i = 1; i < 9; i++) {
			if(colors[i] > max) {
				max = colors[i];
				index = i;
			} else if(colors[i] == max) {
				index = 0;
			}
		}
		return new Point(n, index);
	}
	
	public ConwayGrid(ConwayGame game) {
		_game = game;
		gridChanged = false;
	}
	
	public void setup() {
		_map = new ConcurrentHashMap<Point, Integer>(16, .75f, _game._options.targetThreadCount);
	}

	public static void main (String[] args) throws Exception {
	}

	public void addCell(int x, int y, int t) {
		_map.put(new Point(x,y), t);
	}

	public void addCell(Point p, int t) {
		_map.put(p, t);
	}

	public int getCell(int x, int y) {
		if(_map.containsKey(new Point(x,y))) return _map.get(new Point(x,y)); else return -1;
	}

	public int getCell(Point p) {
		if(_map.containsKey(p)) return _map.get(p); else return -1;
	}

	public int removeCell(Point p) {
		return _map.remove(p);
	}


	public void printBoard() throws Exception{
		for (int i = 0; i < 10; i++) {
			System.out.printf("|");
			for (int j = 0; j < 10; j++) {
				if (_map.containsKey(new Point(i,j))) {
					System.out.printf(" x ");
					System.out.printf("|");
				} else {
					System.out.printf("   ");
					System.out.printf("|");
				}
			}
			System.out.printf("\n");
		}
		System.out.printf("\n");
	}

	public void hash(int steps) {
		//double nearestPowerOf2 = Math.pow(2, Math.round(Math.log(steps)/Math.log(2)));
		if(USE_HASH_ALGORITHM) {
			try {
				steps = (int)Math.round(Math.log(steps) / Math.log(2));
				System.err.printf("Attempting to hash with step size %d.\n", steps);
			} catch (Exception e) {
				System.err.printf("There was a problem with the input to the hash function: %d\n", steps);
				return;
			}
			
			_hasher = new ConwayHash(_game, _map);
			_hasher.inheritTable();
			_hasher.flushTable();
			try {
				_map = _hasher.hash(steps);
				generations += _hasher.generations;
				gridChanged = true;
			} catch (UniverseTooLargeException e) {
				System.err.printf("The requested number of generations was too small for a grid this size: %d\n", (long)Math.pow(2, steps));
			} finally {
				_hasher.flushTable();
			}
			
		} else {
			for (int i = 0; i < steps; i++) {
				step();
			}
		}
	}

	public Point neighborInfo(Point p) {
		return neighborInfo(p, 9);
	}

	public void reset() {
		_map = new ConcurrentHashMap<Point, Integer>(16, .75f, _game.threadCount);
		generations = 0;
		liveCells = new HashMap<Integer, Long>();
		gridChanged = true;
		//coloredCells = new long[9];
	}
	
	public class RunnableEvaluator implements Runnable {
		List<Point> points;
		Map<Point, Integer> newGrid;
		public RunnableEvaluator (List<Point> _points, Map<Point, Integer> _newGrid) {
			points = _points;
			newGrid = _newGrid;
		}
		public void run() {
			for(Point p : points) evalPoint(p, newGrid);    
		}
	}
}
