import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;


public class GameStatusPanel extends JPanel {
	ConwayGame _game;
	public static final int PREFERRED_HEIGHT = 64;
	
	public GameStatusPanel(ConwayGame game) {
		_game = game;
		controlSize();
	}
	
	public void controlSize() {
		setPreferredSize(new Dimension(_game._gui.dim.width, PREFERRED_HEIGHT));
		setMinimumSize(new Dimension(_game._gui.dim.width, PREFERRED_HEIGHT));
	}
	
	//@Override
	public void paintComponent(Graphics page) {
		super.paintComponent(page);
		
		Image bufferImage = createImage(getSize().width, getSize().height);
		Graphics2D buffer = (Graphics2D) (bufferImage.getGraphics());
	
		_game._gui.drawStatus(buffer);
	
		page.drawImage(bufferImage, 0, 0, null);
	}
}