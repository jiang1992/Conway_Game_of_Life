import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;

public class ConwayGame {
	//The GUI object that contains the game itself.
	ConwayGUI _gui;
	//The Object that contains the Input listener for the game.
	ConwayInput _input;
	//The object that contains the grid and helper methods for manipulating it.
	ConwayGrid _grid;
	//The object that contains the options available for the game.
	ConwayOptionGUI _options;
	//Thread that will handle the automatic steps.
	Thread gameRunner;
	
	Object gameLock;
	
	//CONTROLS
	boolean paused;
	double targetGPS;
	double currentGPS;
	long renderTime;
	int threadCount;
	
	
	//Map<Point, Integer> _grid;
	public ConwayGame() {
		//Controls Initialization
		paused = true;
		targetGPS = 60.0;
		threadCount = Runtime.getRuntime().availableProcessors();
		
		//Objects Initialization
		_grid = new ConwayGrid(this);
		_input = new ConwayInput(this);
		_gui = new ConwayGUI(this);
		_options = new ConwayOptionGUI(this);
		
		gameLock = new Object();
		_grid.setup();
		
		//_options.showOptions();
		
		_gui.setup();
		
		_grid.addCell(0,0, 0);
		_grid.addCell(1,0, 0);
		_grid.addCell(2,0, 0);
		_grid.addCell(2,1, 0);
		_grid.addCell(1,2, 0);
		
		gameRunner = new Thread(new Runnable() {
			public void run() {
				gameLoop();
			}
		});
		gameRunner.start();
		
		//_options.birthList.add(3);
		//_options.surviveList.add(2);
		//_options.surviveList.add(3);
	}
	
	public void gameLoop() {
		long startTime = System.nanoTime();
		long maxDelay = (long)(1_000_000_000 / targetGPS);
		long previousRender;
		long lastGPSUpdate = System.nanoTime();
		startTime = System.nanoTime();
		previousRender = startTime;
		Thread current = Thread.currentThread();
		while(gameRunner == current) {
			maxDelay = (_options.targetGPS <= 0) ? 0l : (long)(1_000_000_000 / _options.targetGPS);
			//System.err.printf("We\'re in the logic thread!\n");
			if(!paused) _grid.step();
			
			long endTime = System.nanoTime();
			
			//This process will calculate the actual GPS of the game
			if(paused) {
				currentGPS = 0;
				//renderTime = 0;
			} else {
				renderTime = (long)(renderTime * 0.90 + (endTime - previousRender) * 0.10);
				previousRender = endTime;
				//if(endTime - lastGPSUpdate > 1_000_000_000) {
					currentGPS = Math.round(10_000_000_000l / renderTime) / 10.0;
					lastGPSUpdate = endTime;
				//}
			}
			long timeSleep;
			if(paused) timeSleep = 100_000;
			else timeSleep = ((startTime + maxDelay) - endTime) / 1_000_000;
			if(timeSleep <= 0) {
				startTime = endTime;
			} else {
				startTime += maxDelay;
				synchronized(gameLock) {
					try {
						gameLock.wait(timeSleep);
					} catch (InterruptedException e) {
					}
				}
				//Thread.sleep(timeSleep);
			}
		}
	}
		
	
	public static void main(String[] args) {
		ConwayGame game = new ConwayGame();
	}
}
