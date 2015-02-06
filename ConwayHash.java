import java.util.*;
import java.util.concurrent.*;
import java.awt.*;

public class ConwayHash {
	static final int TABLE_SIZE = 1_000;
	ConwayGame _game;
	Map<Point, Integer> _grid;
	Map<HashNode, HashNode> nodeTable;
	static Map<HashNode, HashNode> inheritableNodeTable = Collections.synchronizedMap(new HashMap<HashNode, HashNode>(TABLE_SIZE, 0.75f));
	//Map<HashNode, HashNode> hashTable;
	//static Map<HashNode, Hashnode> inheritableHashTable = Collections.synchronizedMap(new HashMap<HashNode, HashNode>(100_000_000, 0.75f));
	Set<Integer> birth;
	Set<Integer> survive;
	int maxCount;
	long generations;
	double hashingTotal;
	double hashingStatus;
	
	public ConwayHash(ConwayGame game, Map<Point, Integer> grid) {
		_game = game;
		birth = new HashSet<Integer>(20, 1.0f);
		survive = new HashSet<Integer>(20, 1.0f);
		birth.addAll(_game._options.birthList);
		survive.addAll(_game._options.surviveList);
		maxCount = 0;
		for(int i : birth) maxCount = Math.max(maxCount, i);
		for(int i : survive) maxCount = Math.max(maxCount, i);
		_grid = grid;
		nodeTable = Collections.synchronizedMap(new HashMap<HashNode, HashNode>(TABLE_SIZE, 0.75f));
		//hashTable = Collections.synchronizedMap(new HashMap<HashNode, HashNode>(100_000_000, 0.75f));
		generations = 0;
	}
	
	public void inheritTable() {
		nodeTable = inheritableNodeTable;
		//hashTable = inheritableHashTable;
	}
	
	public void flushTable() {
		nodeTable.clear();
		//hashTable.clear();
	}
	
	public static void flushInheritableTable() {
		inheritableNodeTable.clear();
	}
	
	public Map<Point, Integer> hash(int p) throws UniverseTooLargeException {
		//System.err.printf("In ConwayHash.hash(); about to begin hashing. Will begin by adding cells.\n");
		Map<Point, Integer> newGrid = new ConcurrentHashMap<Point, Integer>(16, 0.75f, _game._options.targetThreadCount);
		//System.err.printf("About to create initial universe.\n");
		HashNode universe = HashNode.create(this);
		//System.err.printf("About to begin adding cells.\n");
		universe = addAllCells(universe);
		//System.err.printf("Cells have all been added.\n");
		if(p > 3) {
			while(universe.level - 2 < p || universe.population != universe.center().population) {
				//System.err.printf("Adjusting universe size.\n");
				universe = universe.expandTree();
			}
			if(universe.level - 2 == p) {
				//System.err.printf("Hashing has begun!\n");
				hashingTotal = Math.pow(4, universe.level);
				hashingStatus = 0;
				universe = universe.nextHash();
				System.err.printf("HashingStatus: %15f\nHashingTotal:  %15f\n", hashingStatus, hashingTotal);
				generations += (long)Math.pow(2, p);
			} else throw new UniverseTooLargeException();
		}
		
		//System.err.printf("Finished hashing; about to begin retrieving points.\n");
		hashingTotal = universe.population;
		hashingStatus = hashingTotal;
		retrievePoints(universe, newGrid, 0,0);
		
		//System.err.printf("Points Retrieved; now done.\n");
		System.err.printf("Size of Node Table: %d.\n", nodeTable.keySet().size());
		return newGrid;
	}
	
	public HashNode addAllCells(HashNode universe) {
		for(Point p : _grid.keySet()) {
			int type = _grid.get(p);
			if(type >= 0) {
				int x = p.x, y = p.y;
				int limit = 1 << (universe.level - 1);
				while(!(-limit <= x && -limit <= y && x < limit && y < limit)) {
					universe = universe.expandTree();
					limit = 1 << (universe.level-1);
				}
				universe = universe.setCell(x,y, type);
			}
		}
		return universe;
	}
	
	public void retrievePoints(HashNode universe, Map<Point, Integer> newGrid, int xffset, int yffset) {
		if(universe == null) return;
		if(universe.level != 0) {
			int offset = (universe.level > 1) ? (int)Math.pow(2,universe.level-2) : 1;
			int fix = (universe.level == 1) ? -1 : 0;
			if(universe.nw != null && universe.nw.population > 0)
				retrievePoints(universe.nw, newGrid, -offset + xffset, -offset + yffset);
			if(universe.ne != null && universe.ne.population > 0)
				retrievePoints(universe.ne, newGrid, offset + xffset + fix, -offset + yffset);
			if(universe.sw != null && universe.sw.population > 0)
				retrievePoints(universe.sw, newGrid, -offset + xffset, offset + yffset + fix);
			if(universe.se != null && universe.se.population > 0)
				retrievePoints(universe.se, newGrid, offset + xffset + fix, offset + yffset + fix);
		} else {
			if(universe.type >= 0) {
				//System.err.printf("    Cell FOUND at %d, %d, type %d.\n", xffset, yffset, universe.type);
				newGrid.put(new Point(xffset, yffset), universe.type);
				hashingStatus -= 1;
			}
		}
	}
}
