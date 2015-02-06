import java.util.*;

public class HashNode {
	public static final boolean DEBUG = false;
	ConwayHash _hash;
	final HashNode nw, sw, ne, se;
	HashNode result;
	
	final int type;
	final int population;
	final int level;
	
	public HashNode instep(int[] data) {
		int neighbors = 0;
		int[] colors = new int[9];
		for(int i = 0; i < colors.length; i++) colors[i] = 0;
		boolean tooMany = false;
		boolean colorFound = false;
		if(DEBUG)
			System.err.printf("{");
		for(int i = 0; i < 9; i++) {
			if(DEBUG)
				System.err.printf(data[i] + ",");
			if(i == 4) continue;
			if(data[i] >= 0) neighbors++;
			if(neighbors > _hash.maxCount) {
				if(DEBUG)
					System.err.printf("-----\n");
				return create(-1);
			}
			if(data[i] > 0) {
				colorFound = true;
				colors[data[i]]++;
			}
		}
		if(DEBUG) {
			System.err.printf("} + {");
			for(int i = 0; i < 9; i++) {
				System.err.printf(colors[i] + ",");
			}
			System.err.printf("} --> ");
		}
		if(data[4] >= 0) {
			if(_hash.survive.contains(neighbors)) {
				if(DEBUG)
					System.err.printf(data[4] + "\n");
				return create(data[4]);
			} else {
				if(DEBUG)
					System.err.printf(-1 + "\n");
				return create(-1);
			}
		} else if(_hash.birth.contains(neighbors)) {
			if(colorFound) {
				int index = 0, max = 0;
				for(int i = 1; i < 9; i++) {
					if(colors[i] > max) {
						max = colors[i];
						index = i;
					} else if(colors[i] == max && i != index) {
						index = 0;
					}
				}
				if(DEBUG)
					System.err.printf(index + "\n");
				return create(index);
			} else return create(0);
		} else {
			if(DEBUG)
				System.err.printf(-1 + "\n");
			return create(-1);
		}
	}
		
	public HashNode step() {
		int[][] data = new int[4][9];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 9; j++) {
				int x = (j%3) + (i%2) - 2;
				int y = (j/3) + (i/2) - 2;
				data[i][j] = getCell(x, y);
			}
		}
		//System.err.printf(data.);
		return create(instep(data[0]), instep(data[1]), instep(data[2]), instep(data[3]));
	}
	
	public HashNode nextHash() {
		if(result != null) {
			_hash.hashingStatus += Math.pow(4,level);
			return result;
		}
		if(population < 1) {
			_hash.hashingStatus += Math.pow(4,level);
			return nw;
		}
		if(level == 2) {
			_hash.hashingStatus += Math.pow(4,level);
			return step();
		}
		HashNode jj = nw.nextHash();
		HashNode jk = horizontalForward(nw, ne);
		HashNode jl = ne.nextHash();
		HashNode kj = verticalForward(nw, sw);
		HashNode kk = centerForward();
		HashNode kl = verticalForward(ne, se);
		HashNode lj = sw.nextHash();
		HashNode lk = horizontalForward(sw, se);
		HashNode ll = se.nextHash();
		_hash.hashingStatus -= Math.pow(4,level) / 4 * 9;
		
		result = create(create(jj,jk,kj,kk).nextHash(),
						create(jk,jl,kk,kl).nextHash(),
						create(kj,kk,lj,lk).nextHash(),
						create(kk,kl,lk,ll).nextHash());
		return result;
	}
	
	public HashNode setCell(int x, int y, int _type) {
		if(level == 0) {
			//System.err.printf("Placing type %d in universe.\n", _type);
			return create(_type);
		}
		
		int adjust = (level > 1) ? (1 << (level - 2)) : 0;
		if(x < 0)
			if(y < 0)
				return create(nw.setCell(x+adjust,y+adjust,_type), ne, sw, se);
			else
				return create(nw, ne, sw.setCell(x+adjust,y-adjust,_type), se);
		else
			if(y < 0)
				return create(nw, ne.setCell(x-adjust,y+adjust,_type), sw, se);
			else
				return create(nw, ne, sw, se.setCell(x-adjust,y-adjust,_type));
			
		
	}
	
	public int getCell(int x, int y) {
		if(level == 0) {
			//if(type >= 0)
				//System.err.printf("Found type %d, level is %d.\n", type, level);
			return type;
		}
		long bounds = 1 << (level - 1);
		if(-bounds > x || x >= bounds || -bounds > y || y >= bounds) {
			System.err.printf("There was an invalid request! %d, %d at level %d.\n", x, y, level);
			return -1;
		}
		int adjust = (level > 1) ? (1 << (level - 2)) : 0;
		if(x < 0) {
			if(y < 0) {
				return nw.getCell(x+adjust,y+adjust);
			} else {
				return sw.getCell(x+adjust,y-adjust);
			}
		} else {
			if(y < 0) {
				return ne.getCell(x-adjust,y+adjust);
			} else {
				return se.getCell(x-adjust,y-adjust);
			}
		}
	}
	
	public HashNode center() {
		return create(nw.se, ne.sw, sw.ne, se.nw);
	}
	
	public HashNode horizontal(HashNode w, HashNode e) {
		return create(w.ne.se, e.nw.sw, w.se.ne, e.sw.nw);
	}
	
	public HashNode vertical(HashNode n, HashNode s) {
		return create(n.sw.se, n.se.sw, s.nw.ne, s.ne.nw);
	}
	
	public HashNode centered() {
		return create(nw.se.se, ne.sw.sw, sw.ne.ne, se.nw.nw);
	}
	
	public HashNode centerForward() {
		return center().nextHash();
	}
	
	public HashNode horizontalForward(HashNode w, HashNode e) {
		return create(w.ne, e.nw, w.se, e.sw).nextHash();
	}
	
	public HashNode verticalForward(HashNode n, HashNode s) {
		return create(n.sw, n.se, s.nw, s.ne).nextHash();
	}
	
	public HashNode create(int _type) {
		return new HashNode(_hash, _type).intern();
	}
	
	public HashNode create(HashNode _nw, HashNode _ne, HashNode _sw, HashNode _se) {
		return new HashNode(_hash, _nw, _ne, _sw, _se).intern();
	}
	
	public static HashNode create(ConwayHash hash) {
		return new HashNode(hash, -1).emptyTree(3);
	}
	
	public HashNode intern() {
		HashNode temp = _hash.nodeTable.get(this);
		if(temp != null) return temp;
		_hash.nodeTable.put(this,this);
		return this;
	}
	
	public HashNode emptyTree(int _level) {
		if(_level == 0) return create(-1);
		HashNode empty = emptyTree(_level - 1);
		return create(empty, empty, empty, empty);
	}
	
	public HashNode expandTree() {
		HashNode padding = emptyTree(level - 1);
		return create(create(padding, padding, padding, nw),
					  create(padding, padding, ne, padding),
					  create(padding, sw, padding, padding),
					  create(se, padding, padding, padding));
	}
	
	private HashNode(ConwayHash hash, int _type) {
		_hash = hash;
		type = _type;
		if(type >= 0)
			population = 1;
		else 
			population = 0;
		result = null;
		level = 0;
		nw = null;
		sw = null;
		ne = null;
		se = null;
	}
	
	private HashNode(ConwayHash hash, HashNode _nw, HashNode _ne, HashNode _sw, HashNode _se) {
		_hash = hash;
		population = _nw.population + _ne.population + _sw.population + _se.population;
		type = population > 0 ? 0 : -1;
		result = null;
		level = _nw.level + 1;
		nw = _nw;
		sw = _sw;
		ne = _ne;
		se = _se;
	}
	
	public boolean equals(Object o) {
		HashNode n = (HashNode) o;
		if(n == this) return true;
		if(level != n.level) return false;
		if(level == 0) return type == n.type;
		return nw == n.nw && ne == n.ne && sw == n.sw && se == n.se;
	} 
	
	public int hashCode() {
		if(level == 0) return type;
		return System.identityHashCode(nw) +
			   System.identityHashCode(ne) * 11 +
			   System.identityHashCode(sw) * 101 + 
			   System.identityHashCode(se) * 1007;
	}
}
