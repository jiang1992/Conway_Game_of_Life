import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;

public class ConwayGUI extends JFrame {
	//The Object that contains the game itself.
	ConwayGame _game;
	//The Object that contains the viewing region of the game.
	GamePanel _panel;
	//The Object that contains the status bar of the game.
	GameStatusPanel _statusPanel;
	//The rendering Thread will attempt to maintain the Frames per Second of the screen 
	double targetFPS;
	//The system will keep track of the actual FPS of the system and store it here.
	double currentFPS;
	long renderTime;

	static final Color PURPLE = new Color(180,0,200);
	static final Color YELLOW = new Color(255,215,0);
	static final Color OLIVE = new Color(128,128,0);
	static final Color ORANGE = new Color(255,140,0);
	static final Color CROSSHAIR_PINK = new Color(255,128,128,128);
	
	static final Map<Integer, Color> colors = new HashMap<Integer, Color>();
	static final Map<Integer, String> colorNames = new HashMap<Integer, String>();
	static {
		colors.put(0, Color.BLACK);
		colors.put(1, Color.RED);
		colors.put(2, Color.BLUE);
		colors.put(3, YELLOW);
		colors.put(4, Color.MAGENTA);
		colors.put(5, OLIVE);
		colors.put(6, Color.CYAN);
		colors.put(7, ORANGE);
		colors.put(8, Color.GREEN);
		colors.put(-1, Color.LIGHT_GRAY);
		colors.put(-2, Color.GRAY);
		colors.put(-3, Color.DARK_GRAY);
		
		colorNames.put(0, "Black");
		colorNames.put(1, "Red");
		colorNames.put(2, "Blue");
		colorNames.put(3, "Yellow");
		colorNames.put(4, "Magenta");
		colorNames.put(5, "Olive");
		colorNames.put(6, "Cyan");
		colorNames.put(7, "Orange");
		colorNames.put(8, "Green");
		colorNames.put(-1, "Light Gray (Does not interact with grid)");
		colorNames.put(-2, "Gray (Does not interact with grid)");
		colorNames.put(-3, "Dark Gray (Does not interact with grid)");
	}
	//BufferedImage _screen;

	//CONTROLS
	double viewX, viewY;
	int viewZoom;
	int guideSize;
	boolean viewGuidelines;

	Dimension dim;
	//The Thread that handles redrawing of the screen
	Thread paintRunner;
	BufferedImage savedGrid;

	public ConwayGUI(ConwayGame game) {
		_game = game;
		setTitle("Conway\'s Game of Life");
		setLocation(0, 0);
		setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void setup() {

		_panel = new GamePanel(_game);
		_panel.setOpaque(true);

		dim = _panel.getSize();

		_statusPanel = new GameStatusPanel(_game);
		_statusPanel.setOpaque(true);

		getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().add(_panel, BorderLayout.CENTER);
		getContentPane().add(_statusPanel, BorderLayout.SOUTH);
		targetFPS = 60.0;
		setVisible(true);
		setListeners(_game._input);
		viewX = 0;
		viewY = 0;
		viewZoom = 1;
		guideSize = 0;
		viewGuidelines = true;

		paintRunner = new Thread(new Runnable() {
		public void run() {
		threadLoop();
		}
		});
		paintRunner.start();
	}

	public void drawGame(Graphics2D page) {
		boolean rerender = false;
		rerender = checkResize();
		if(_game._grid.gridChanged) {
			rerender = true;
			_game._grid.gridChanged = false;
		} else if(_game._input.stateChanged) {
			rerender = true;
			_game._input.stateChanged = false;
		}
		
		if(savedGrid == null) rerender = true;
			
			
		//BufferedImage screen = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		//Graphics2D page = (Graphics2D)screen.getGraphics();

		int ox = (int)viewX;
		int oy = (int)viewY;
		int oz = viewZoom;
		int og = guideSize;

		//cx and cy are the centers of the workable screen.
		int cx = dim.width / 2;
		int cy = dim.height / 2;
		Map<Point, Integer> grid = _game._grid._map;
		Set<Point> points = grid.keySet();
		Rectangle2D rect = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
		if(rerender) {
			savedGrid = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D _page = (Graphics2D)savedGrid.getGraphics();
			_page.setColor(Color.WHITE);
			rect.setFrame(0,0,dim.width, dim.height);
			_page.fill(rect);
			_page.setColor(Color.BLACK);
			for(Point p : points) {
				int color = grid.get(p);
				Color c = Color.BLACK;
				if(colors.containsKey(color)) c = colors.get(color);
				_page.setColor(c);
				rect.setFrame(cx + (p.x  + ox) * oz, cy + (p.y + oy) * oz, oz, oz);
				_page.fill(rect);
			}
		} page.drawImage(savedGrid,0,0,null);
			

		if(viewGuidelines) {     
			Point mp = getRelativePoint(_game._input.mouseX, _game._input.mouseY); 
			if (_game._input.pattern.size() <= 1) {
				page.setColor(CROSSHAIR_PINK);
				rect.setFrame(cx + (mp.x + ox - og) * oz, cy + (mp.y + oy) * oz, (og * 2 + 1) * oz, oz);
				page.fill(rect);
				rect.setFrame(cx + (mp.x + ox) * oz, cy + (mp.y + oy - og) * oz, oz, (og * 2 + 1) * oz);
				page.fill(rect);
				//fill horizontal crosshair
				/*for (int i = 0; i < width; i++) {
					p = new Point(0 - cp.x - (width/2) + i, mp.y);
					//if there's a point there already, draw dark pink, other wise draw pink
					if (_game._grid._map.containsKey(p)) {
						page.setColor(new Color(176, 48, 96));
					} else {
						page.setColor(Color.PINK);  
					}
					rect.setFrame(cx + (p.x  + ox) * oz, cy + (p.y + oy) * oz, oz, oz);
					page.fill(rect);
				}
				//fill vertical crosshair
				for (int i = 0; i < height; i++) {
					p = new Point(mp.x, 0 - cp.y - (height/2) + i);
					//if there's a point there already, draw dark pink, other wise draw pink
					if (_game._grid._map.containsKey(p)) {
						page.setColor(new Color(176, 48, 96));
					} else {
						page.setColor(Color.PINK);  
					}
					rect.setFrame(cx + (p.x  + ox) * oz, cy + (p.y + oy) * oz, oz, oz);
					page.fill(rect);
				}*/
				//page.setColor(Color.PINK);  

			} else {
				//if pattern is selected, draw pattern
				page.setColor(Color.PINK);   
				for(Point p : _game._input.pattern) {
					p = new Point(mp.x + p.x, mp.y + p.y);

					rect.setFrame(cx + (p.x  + ox) * oz, cy + (p.y + oy) * oz, oz, oz);
					page.fill(rect);
				}
			}

			if(_game.paused && oz > 2) {
				page.setColor(Color.LIGHT_GRAY);
				for(int i = cx; i < dim.width; i+= oz) {
					rect.setFrame(i, 0, 1, dim.height);
					page.fill(rect);
					rect.setFrame(dim.width - i, 0, 1, dim.height);
					page.fill(rect);
				}
				for(int i = cy; i < dim.height; i+= oz) {
					rect.setFrame(0, i, dim.width, 1);
					page.fill(rect);
					rect.setFrame(0, dim.height - i, dim.width, 1);
					page.fill(rect);
				}
			}
		}

		//_page.drawImage(screen, 0, 0, null);
		//_screen = screen;
	}

	public void drawStatus(Graphics2D page) {
		checkResize();
		String outString;
		Map<Point, Integer> grid = _game._grid._map;
		Map<Integer, Long> liveCells = _game._grid.liveCells;

		page.setColor(Color.LIGHT_GRAY);
		page.fillRect(0,0, dim.width, GameStatusPanel.PREFERRED_HEIGHT);

		page.setColor(Color.BLACK);

		page.drawString(String.format("Current F/s: %04.1f (%4d)", currentFPS, _game._options.targetFPS), 0, 36);
		if(_game._options.targetGPS > 0) {
			page.drawString(String.format("Current Gens/s: %04.1f (%4d)", _game.currentGPS, _game._options.targetGPS), 200, 36);
		} else {
			page.drawString(String.format("Current Gens/s: %04.1f (Unbounded)", _game.currentGPS), 200, 36);
		}
			

		page.drawString(String.format("Current Generation: %,8d", _game._grid.generations), 0, 12);

		if(ConwayGrid.CHECK_CELL_COUNT) {
			if(liveCells != null && liveCells.containsKey(null)) {
				page.drawString(String.format("Number of Cells: %d", liveCells.get(null)), 200, 12);
			} else {
				page.drawString(String.format("Number of Cells: %d", 0), 200, 12);
			}
		} else {
			page.drawString(String.format("Number of Cells: %d", grid.size()), 200, 12);
		}

		page.drawString(String.format("Current (x,y): (%.1f, %.1f)", -viewX, viewY), 400, 12);

		Point p = getRelativePoint(_game._input.mouseX, _game._input.mouseY);

		page.drawString(String.format("Mouse (x,y): (%d, %d)", p.x, -1 * p.y), 400, 24);

		page.drawString(String.format("Current status: %s", _game.paused? "paused": "running"), 600, 12);

		page.drawString(String.format("Current zoom level: %d", viewZoom), 800, 12);

		if (_game._input.hashing) {
			//page.drawString(String.format("Hashing: true"), 600, 24);
			try {
				page.drawString(String.format("Hashing: %3.10f%%", (_game._grid._hasher.hashingStatus / _game._grid._hasher.hashingTotal + 1.0/3) / 4.0/3 * 100), 600, 24);
				//System.err.printf("Hashing Status: %f\nHashing Total: %f\n", _game._grid._hasher.hashingStatus, _game._grid._hasher.hashingTotal);
			} catch (Exception e) {
				page.drawString(String.format("Hashing: Error"), 600, 24);
			}
		} else {
			page.drawString(String.format("Hashing: false"), 600, 24);
		}
		
		page.drawString("Press escape to open up the options window", 1000, 60);

		String mouseColor = "Unknown";
		Color c = Color.BLACK;
		if(colors.containsKey(_game._input.colorMode) && _game._input.colorMode >= 0) c = colors.get(_game._input.colorMode);
		page.setColor(c);
		
		if(colorNames.containsKey(_game._input.colorMode)) mouseColor = colorNames.get(_game._input.colorMode);
		page.drawString("Current Mouse Color: " + mouseColor, 800, 24);
	}

	public Point getRelativePoint(int x, int y) {
		return new Point((x - dim.width/2) / viewZoom - (int)viewX + ((x-dim.width/2 < 0) ? -1 : 0), 
				 (y - dim.height/2) / viewZoom - (int)viewY + ((y-dim.height/2 < 0) ? -1 : 0));
	}
	
	public int[][] getBoundaries() {
		int[][] bounds = new int[2][2];
		bounds[0][0] = (int)viewX - dim.width / viewZoom / 2;
		bounds[0][1] = (int)viewX + dim.width / viewZoom / 2;
		bounds[1][0] = (int)viewY - dim.height / viewZoom / 2;
		bounds[1][1] = (int)viewY + dim.height / viewZoom / 2;
		return bounds;
	}

	public boolean checkResize() {
		if(_panel.getSize().width != dim.width || _panel.getSize().height != dim.height) {
			System.err.printf("Adjusting for resize.\n");
			dim = _panel.getSize();
			_statusPanel.controlSize();
			return true;
		}
		return false;
	}

	long lastFPSUpdate;
	public void threadLoop() {
		long startTime = System.nanoTime();
		long maxDelay = (long)(1_000_000_000 / _game._options.targetFPS);
		long previousRender;
		startTime = System.nanoTime();
		previousRender = startTime;
		Thread current = Thread.currentThread();
		while(paintRunner == current) {
			maxDelay = (long)(1_000_000_000 / _game._options.targetFPS);
			//System.err.printf("We\'re in the Drawing Thread!\n");
			_panel.repaint();
			_statusPanel.repaint();
			long endTime = System.nanoTime();

			//This process will calculate the actual FPS of the game
			renderTime = (long)(renderTime * 0.90 + (endTime - previousRender) * 0.10);
			previousRender = endTime;

			Calendar now = Calendar.getInstance();
			if(lastFPSUpdate != now.get(Calendar.SECOND)) {
				currentFPS = Math.round(10_000_000_000l / renderTime) / 10.0;
				lastFPSUpdate = now.get(Calendar.SECOND);
			}

			//If it's taking really long to render frames, we'll simply skip to the next frame immediately; otherwise, we wait to stabilize the fps near the target.
			if(endTime - startTime > maxDelay) {
				startTime = endTime;
			} else {
				startTime += maxDelay;
				try {
					long timeSleep = (startTime - endTime) / 1_000_000;
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
				}	
			}
		}
	}

	void setListeners(ConwayInput input) {
		_panel.addMouseListener(input);
		_panel.addMouseMotionListener(input);
		_panel.addMouseWheelListener(input);
		addKeyListener(input);
	}
}
