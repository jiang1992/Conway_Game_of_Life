
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;

public class ConwayPattern {

	public ConwayPattern() {}

	public static Set<Point> parsePattern(File patternFile) throws FileNotFoundException{
		Set<Point> pattern = new HashSet<Point>();
		Scanner scan = new Scanner(patternFile);
		String line = scan.nextLine();
		if(line.equals("#Life 1.06")) {
			int i = 0; int temp = 0;
			while(scan.hasNext()) {
				int value = scan.nextInt();
				if((++i)%2 == 0) {
					pattern.add(new Point(temp, value));
				} else {
					temp = value;
				}
			}
		} else {
			while(!line.contains("x = ") || line.substring(0,1).equals("#")) {
				line = scan.nextLine();
			}
			line = line.replace(',', ' ');
			Scanner parser = new Scanner(line);
			
			int xSize = 0;
			while(xSize == 0 && parser.hasNext()) {
				if(parser.hasNextInt()) {
					xSize = parser.nextInt();
				} else {
					parser.next();
				}
			}
			int ySize = 0;
			while(ySize == 0 && parser.hasNext()) {
				if(parser.hasNextInt()) {
					ySize = parser.nextInt();
				} else {
					parser.next();
				}
			}
			int[][] points = new int[ySize][xSize];
			int xindex = 0;
			int yindex = 0;
			line = scan.nextLine();
			while(scan.hasNext()) line += scan.nextLine();
			int index = 0;
			int sindex = 0;
			int eindex = line.indexOf('!');
			int lindex = line.indexOf('$');
			do {
				int bindex = line.indexOf('b', sindex);
				int oindex = line.indexOf('o', sindex);
				int mode;
				if(bindex == -1 && oindex == -1) break;
				else if(oindex == -1) {
					index = bindex;
					mode = 0;
				} else if(bindex == -1) {
					index = oindex;
					mode = 1;
				} else {
					index = Math.min(bindex, oindex);
					mode = (bindex < oindex) ? 0 : 1;
				}
				if(index > eindex) break;
				if (index > lindex && lindex != -1) {
					if(sindex == lindex) {
						sindex = lindex + 1;
						lindex = line.indexOf('$', sindex);
						xindex = 0;
						yindex++;
					} else {
						int offset = Integer.parseInt(line.substring(sindex, lindex));
						yindex += offset;
						sindex = lindex + 1;
						lindex = line.indexOf('$', sindex);
						xindex = 0;
					}
				} else if(sindex == index) {
					if(mode == 1) {
						points[yindex][xindex] = 1;
					}
					xindex++;
					sindex = index + 1;
				} else {
					int offset = Integer.parseInt(line.substring(sindex, index));
					if(mode == 1) {
						for(int i = 0; i < offset; i++) {
							points[yindex][xindex++] = 1;
						}
					} else {
						xindex += offset;
					}
					sindex = index + 1;
				}
			} while(index < eindex);
			for(int i = 0; i < ySize; i++) {
				for(int j = 0; j < xSize; j++) {
					if(points[i][j] == 1) {
						pattern.add(new Point(j - xSize/2, i - ySize/2));
					}
				}
			}
		}
		return pattern;
	}
	
	public static void writePattern(File patternFile, Map<Point, Integer> grid) throws IOException {
		patternFile.createNewFile();
		int mix = 0, max = 0, miy = 0, may = 0;
		boolean first = true;
		for(Point p : grid.keySet()) {
			if(grid.get(p) < 0) continue;
			if(first) {
				first = false;
				mix = p.x;
				max = p.x;
				miy = p.y;
				may = p.y;
				continue;
			}
			mix = Math.min(mix, p.x);
			max = Math.max(max, p.x);
			miy = Math.min(miy, p.y);
			may = Math.max(may, p.y);
		}
		int sx = max - mix + 1;
		int sy = may - miy + 1;
		boolean[][] ogrid = new boolean[sx][sy];
		
		for(Point p : grid.keySet()) {
			if(grid.get(p) < 0) continue;
			ogrid[p.x - mix][p.y - miy] = true;
		}
		PrintWriter out = new PrintWriter(patternFile);
		out.printf("#C A custom pattern saved by the java program.\n");
		System.err.printf("#C A custom pattern saved by the java program.\n");
		out.printf("x = %d, y = %d, rule = 23/3\n", sx, sy);
		System.err.printf("x = %d, y = %d, rule = 23/3\n", sx, sy);
		boolean mode = ogrid[0][0];
		int count = 0;
		int charcount = 0;
		for(int j = 0; j < sy; j++) {
			mode = ogrid[0][j];
			count = 0;
			for(int i = 0; i < sx; i++) {
				if(ogrid[i][j] != mode) {
					if(count > 1) {
						out.printf("%d", count);
						while(count > 0) {
							charcount++;
							count /= 10;
						}
						//System.err.printf("%d\n", count);
					}
					out.printf(((mode) ? "o" : "b"));
					charcount++;
					//System.err.printf(((mode) ? "o\n" : "b\n"));
					mode = ogrid[i][j];
					count = 0;
				}
				if(charcount > 60) {
					out.printf("\n");
					charcount = 0;
				}
				count++;
			}
			if(count > 1) {
				out.printf("%d", count);
				//System.err.printf("%d\n", count);
			}
			out.printf(((mode) ? "o" : "b"));
			//System.err.printf(((mode) ? "o\n" : "b\n"));
			out.printf("$");
			//System.err.printf("$\n");
		}
		out.printf("!");
		//System.err.printf("!\n");
		out.flush();
		out.close();
	}

}