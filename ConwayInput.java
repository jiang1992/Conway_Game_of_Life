/**
  * ConwayInput.java
  *	
  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import javax.imageio.*;

public class ConwayInput implements MouseInputListener, MouseWheelListener, KeyListener {
	ConwayGame _game;
	
	int mouseX; int mouseY;
	public static final int MASK_CONTROL = 1 << 0;
	public static final int MASK_SHIFT = 1 << 1;
	public static final int MASK_ALT = 1 << 2;
	public static final int MASK_UP = 1 << 3;
	public static final int MASK_DOWN = 1 << 4;
	public static final int MASK_LEFT = 1 << 5;
	public static final int MASK_RIGHT = 1 << 6;
	public static final int MASK_PLUS = 1 << 0;
	public static final int MASK_MINUS = 1 << 1;
	
	public static final double MOVES_PER_SECOND = 60;
	public static final int REPEAT_DELAY = 20;
	int moveMask;
	int guideMask;
	int duration;
	int colorMode;
	int drawSize;
	Set<Point> pattern;
	boolean stateChanged;
	
	Thread moveRunner;
	Thread hashingThread;
	//public static final int MASK_CONTROL = 1 << 0;

	boolean hashing;

	public ConwayInput(ConwayGame game) {
		_game = game;
		moveMask = 0;
		guideMask = 0;
		duration = 0;
		colorMode = 0;
		drawSize = 11;
		pattern = new HashSet<Point>();
		pattern.add(new Point(0,0));
		hashing = false;
		hashingThread = null;
		stateChanged = false;
		
		moveRunner = new Thread(new Runnable() {
			public void run() {
				moveLoop();
			}
		});
		moveRunner.start();
	}
	
	public void handleMouseClick(Point p) {
		
		p = _game._gui.getRelativePoint(p.x, p.y);
		
		boolean removal = false;
		if((moveMask & MASK_SHIFT) != 0) removal = true;
		int size = 1;
		if((moveMask & MASK_CONTROL) != 0 && pattern.size() <= 1) size = _game._gui.guideSize * 2 + 1;
		
		if((moveMask & (MASK_ALT)) == 0) {
			for(Point pp : pattern) {
				for(int x = size / -2; x <= size/2; x++) { for(int y = size / -2; y <= size/2; y++) {
					Point ap = new Point(p.x + x + pp.x, p.y + y + pp.y);
					if(!removal)
						_game._grid.addCell(ap, colorMode);
					else {
						try {
							_game._grid.removeCell(ap);
						} catch (NullPointerException e) {}
					}
				}}
			}
		} else if((moveMask & MASK_ALT) != 0) {
			//TODO: handle logic for drawing entire screen. May reconsider this.
		}
		stateChanged = true;
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		//click to add cell		
		if(!_game.paused) return;
		if(e.getButton() != MouseEvent.BUTTON1) return;
		Point p = e.getPoint();
		mouseX = p.x;
		mouseY = p.y;
		
		handleMouseClick(p);
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {
		//click to add cell		
		if(!_game.paused) return;
		//if(e.getButton() != MouseEvent.BUTTON1) return;
		Point p = e.getPoint();
		mouseX = p.x;
		mouseY = p.y;
		
		handleMouseClick(p);
	}
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		mouseX = p.x;
		mouseY = p.y;
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		int tempZoom = _game._gui.viewZoom;
		if((moveMask & MASK_SHIFT) != 0) {
			tempZoom-=notches;
		} else {
			if(notches < 0)
				tempZoom*= 2;
			else 
				tempZoom/= 2;
		}
		if(tempZoom < 1) tempZoom = 1;
		if(tempZoom > 128) tempZoom = 128;
		_game._gui.viewZoom = tempZoom;
		stateChanged = true;
	}
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		Set<Point> tempPattern;
		File directory;
		
		switch(keyCode) {
			case KeyEvent.VK_SPACE:
				if(!_game.paused || hashing) break;
				if(_game._options.isVisible()) break;
				_game._grid.step();
				break;
			case KeyEvent.VK_F4:
				if(!_game.paused || hashing) break;
				if(_game._options.isVisible()) break;
				System.err.printf("About to ask the user for input on the hash algorithm.\n");
				hashing = true;
				String hashResult = JOptionPane.showInputDialog("Enter the number of generations to hash; this number will be rounded to the nearest value of 2.");
				int generations = 0;
				try {
					generations = Integer.parseInt(hashResult);
				} catch (NumberFormatException ex) {
					System.err.printf("User did not enter a number: %s\n", hashResult);
					hashing = false;
				}
				if(hashing) {
					HashingProcess hp = new HashingProcess(generations);
					hashingThread = new Thread(hp);
					hashingThread.start();
				}
							
				break;
			case KeyEvent.VK_C:
				if((moveMask & MASK_CONTROL) != 0) {
					colorMode = 0;
					break;
				}
				if((moveMask & MASK_SHIFT) != 0) {
					if(colorMode > 0 && colorMode <= 8) colorMode--;
					else if(colorMode == 0) colorMode = -3;
					else if(colorMode < -1 && colorMode >= -3) colorMode++;
					else colorMode = 8;
				} else {
					if(colorMode >= 0 && colorMode < 8) colorMode++;
					else if(colorMode >= 8) colorMode = -1;
					else if(colorMode < 0 && colorMode > -3) colorMode--;
					else colorMode = 0;
				}
				break;
			case KeyEvent.VK_ENTER:
				if(_game._options.isVisible() || hashing) break;
				_game.paused = !_game.paused;
				synchronized(_game.gameLock) {
					_game.gameLock.notifyAll();
				}
				break;
			case KeyEvent.VK_R:
				tempPattern = new HashSet<Point>();
				for(Point p : pattern) {
					tempPattern.add(new Point(-1 * p.y,p.x));
				}
				pattern = tempPattern;
				break;
			case KeyEvent.VK_F:
				tempPattern = new HashSet<Point>();
				for(Point p : pattern) {
					tempPattern.add(new Point(-1 * p.x,p.y));
				}
				pattern = tempPattern;
				break;
			case KeyEvent.VK_V:
				tempPattern = new HashSet<Point>();
				for(Point p : pattern) {
					tempPattern.add(new Point(p.x,-1 * p.y));
				}
				pattern = tempPattern;
				break;
			case KeyEvent.VK_H:
				pattern = new HashSet<Point>();
				for(int i = 0; i < _game._gui.guideSize; i++) {
					pattern.add(new Point(i,0));
					pattern.add(new Point(-i,0));
				}
				break;
			case KeyEvent.VK_J:
				pattern = new HashSet<Point>();
				for(int i = 0; i < _game._gui.guideSize; i++) {
					pattern.add(new Point(i,i));
					pattern.add(new Point(-i,-i));
				}
				break;
			case KeyEvent.VK_LEFT:
				if(duration == 0)
					_game._gui.viewX+=1;
				moveMask |= MASK_LEFT;
				stateChanged = true;
				break;
			case KeyEvent.VK_RIGHT:
				if(duration == 0)
					_game._gui.viewX-=1;
				moveMask |= MASK_RIGHT;
				stateChanged = true;
				break;
			case KeyEvent.VK_UP:
				if(duration == 0)
					_game._gui.viewY+=1;
				moveMask |= MASK_UP;
				stateChanged = true;
				break;
			case KeyEvent.VK_DOWN:
				if(duration == 0)
					_game._gui.viewY-=1;
				moveMask |= MASK_DOWN;
				stateChanged = true;
				break;
			case KeyEvent.VK_CONTROL:
				moveMask |= MASK_CONTROL;
				break;
			case KeyEvent.VK_ALT:
				e.consume();
				moveMask |= MASK_ALT;
				break;
			case KeyEvent.VK_SHIFT:
				moveMask |= MASK_SHIFT;
				break;
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_EQUALS:
				if(duration == 0)
					if(_game._gui.guideSize < 20) _game._gui.guideSize+=1;
					else _game._gui.guideSize = _game._gui.guideSize * 21 / 20;
				guideMask |= MASK_PLUS;
				break;
			case KeyEvent.VK_MINUS:
				if(duration == 0 && _game._gui.guideSize > 0)
					if(_game._gui.guideSize < 20 ) _game._gui.guideSize-=1;
					else _game._gui.guideSize = _game._gui.guideSize * 20 / 21;
				guideMask |= MASK_MINUS;
				break;
			case KeyEvent.VK_HOME:
				_game._gui.viewY = 0;
				_game._gui.viewX = 0;
				_game._gui.viewZoom = 1;
				stateChanged = true;
				break;
			case KeyEvent.VK_END:
				JPanel cPanel = new JPanel(new GridLayout(2,2));
				JTextField xField = new JTextField("0", 9);
				JTextField yField = new JTextField("0", 9);
				/*for(ActionListener l : xField.getActionListeners()) xField.removeActionListener(l);
				for(ActionListener l : yField.getActionListeners()) yField.removeActionListener(l);
				KeyListener numbersOnly = new KeyListener() {
					public void keyPressed(KeyEvent ev) {
						int keyCode = ev.getKeyCode();
						switch (keyCode) {
							case KeyEvent.VK_0:
							case KeyEvent.VK_1:
							case KeyEvent.VK_2:
							case KeyEvent.VK_3:
							case KeyEvent.VK_4:
							case KeyEvent.VK_5:
							case KeyEvent.VK_6:
							case KeyEvent.VK_7:
							case KeyEvent.VK_8:
							case KeyEvent.VK_9:
								break;
							default:
								ev.consume();
						}
					}
					public void keyTyped(KeyEvent e){}
					public void keyReleased(KeyEvent e){}
				};
				xField.addKeyListener(numbersOnly);
				yField.addKeyListener(numbersOnly);*/
				JLabel xLabel = new JLabel("x");
				JLabel yLabel = new JLabel("y");
				
				cPanel.add(xLabel);
				cPanel.add(yLabel);
				cPanel.add(xField);
				cPanel.add(yField);
				
				int result = JOptionPane.showConfirmDialog(null, cPanel, "Please Enter the x and y coordinates you would like to jump to.", JOptionPane.OK_CANCEL_OPTION);
				
				if(result == JOptionPane.OK_OPTION)
					try {
						int sx = 0, sy = 0;
						sx = Integer.parseInt(xField.getText());
						sy = Integer.parseInt(yField.getText());
						_game._gui.viewX = -sx;
						_game._gui.viewY = sy;
					} catch (NumberFormatException ex) {
						System.err.printf("User did not enter numbers: {%30s}, {%30s}\n", xField.getText(), yField.getText());
					}
				stateChanged = true;
				break;
			case KeyEvent.VK_DELETE:
				if(!_game.paused) break;
				_game._gui.viewY = 0;
				_game._gui.viewX = 0;
				_game._grid.reset();
				break;
			case KeyEvent.VK_ESCAPE:
				if(!_game.paused) break;
				_game._options.showOptions();
				break;
			case KeyEvent.VK_BACK_SLASH:
				//TODO: Show the Options GUI and force the game to pause.
				_game._gui.viewGuidelines = !_game._gui.viewGuidelines;
				break;
			case KeyEvent.VK_F2:
				directory = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Screenshots");
				if(!directory.exists() || !directory.isDirectory()) directory.mkdir();
				try {
					Calendar now = Calendar.getInstance();
					String fileName = String.format("%04d-%02d-%02d_%02d-%02d-%02d.png", now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
																						 now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
					File outputFile = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Screenshots" + System.getProperty("file.separator") + fileName);
					ImageIO.write(_game._gui._panel._screen, "png", outputFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				pattern = new HashSet<Point>();
				pattern.add(new Point(0,0));
				_game._gui.guideSize = 0;
				break;
			case KeyEvent.VK_INSERT:
				directory = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Patterns");
				if(!directory.exists() || !directory.isDirectory()) directory.mkdir();
				
				JFileChooser chooser = new JFileChooser(directory);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Life Pattern Files", "lif", "life", "rle");
				chooser.setFileFilter(filter);
				int acceptValue = chooser.showOpenDialog(null);
				if(acceptValue == JFileChooser.APPROVE_OPTION) {
					try {
						File file = chooser.getSelectedFile();
						pattern = ConwayPattern.parsePattern(file);
					} catch (Exception ex) {
						System.err.printf("\n\nThere was a problem parsing the file.\n");
						ex.printStackTrace();
					}
				}
				break;

			case KeyEvent.VK_F12:
				e.consume();
				directory = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Patterns");
				if(!directory.exists() || !directory.isDirectory()) directory.mkdir();
				chooser = new JFileChooser(directory);
				acceptValue = chooser.showSaveDialog(null);
				if(acceptValue == JFileChooser.APPROVE_OPTION) {
					try {
						File file = chooser.getSelectedFile();
						String path = file.getAbsolutePath();
						if(!path.endsWith(".rle")) path = path + ".rle";
						file = new File(path);
						ConwayPattern.writePattern(file, _game._grid._map);
					} catch (IOException ex) {
						System.err.printf("File was not successfully written.\n");
						ex.printStackTrace();
					}
				}

				break;
			default:
				break;
		}
	}	
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		
		switch(keyCode) {
			case KeyEvent.VK_LEFT:
				moveMask &= ~MASK_LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				moveMask &= ~MASK_RIGHT;
				break;
			case KeyEvent.VK_UP:
				moveMask &= ~MASK_UP;
				break;
			case KeyEvent.VK_DOWN:
				moveMask &= ~MASK_DOWN;
				break;
			case KeyEvent.VK_CONTROL:
				moveMask &= ~MASK_CONTROL;
				break;
			case KeyEvent.VK_ALT:
				moveMask &= ~MASK_ALT;
				break;
			case KeyEvent.VK_SHIFT:
				moveMask &= ~MASK_SHIFT;
				break;
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_EQUALS:
				guideMask &= ~MASK_PLUS;
				break;
			case KeyEvent.VK_MINUS:
				guideMask &= ~MASK_MINUS;
				break;
			default:
				break;
		}
	}
	
	public void handleMovement() {
		if(moveMask == 0 && guideMask == 0) {
			duration = 0;
			return;
		}
		int currMask = moveMask;
		int currGMask = guideMask;
		
		
		if(duration >= REPEAT_DELAY) {
			if((currGMask & MASK_PLUS) != 0 && _game._gui.guideSize < 2000)
				if(_game._gui.guideSize < 20 || (currMask & MASK_SHIFT) != 0) _game._gui.guideSize+=1;
				else _game._gui.guideSize = _game._gui.guideSize * 21 / 20;
			if((currGMask & MASK_MINUS) != 0 && _game._gui.guideSize > 0)
				if(_game._gui.guideSize < 20 || (currMask & MASK_SHIFT) != 0) _game._gui.guideSize-=1;
				else _game._gui.guideSize = _game._gui.guideSize * 20 / 21;
			double delta = (duration - REPEAT_DELAY) / 100.0;
			if((currMask & (MASK_CONTROL | MASK_SHIFT)) == (MASK_SHIFT | MASK_CONTROL)) { delta = 50; }
			else if((currMask & MASK_CONTROL) != 0) delta *= 5;
			else if((currMask & MASK_SHIFT) != 0) { delta = 0.1; }
			if((currMask & MASK_LEFT) != 0) _game._gui.viewX+=delta;
			if((currMask & MASK_RIGHT) != 0) _game._gui.viewX-=delta;
			if((currMask & MASK_UP) != 0) _game._gui.viewY+=delta;
			if((currMask & MASK_DOWN) != 0) _game._gui.viewY-=delta;
		}
		if((currMask & (MASK_LEFT | MASK_RIGHT | MASK_UP | MASK_DOWN)) != 0) stateChanged = true;
		duration++;
	}
		
	
	public void moveLoop() {
		long startTime = System.nanoTime();
		long maxDelay = (long)(1_000_000_000 / MOVES_PER_SECOND);
		Thread current = Thread.currentThread();
		while(moveRunner == current) {
			handleMovement();
			long endTime = System.nanoTime();
			
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
	
	private class HashingProcess implements Runnable {
		int generations;
		
		HashingProcess(int gen) {
			generations = gen;
		}
		
		public void run() {
			try {
				_game._grid.hash(generations);
			} catch (Exception e) {
				e.printStackTrace();
			}
			hashing = false;
		}
	}
}
