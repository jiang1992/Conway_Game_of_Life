import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;


public class GamePanel extends JPanel {
	ConwayGame _game;
	BufferedImage _screen;
	
	public GamePanel(ConwayGame game) {
		_game = game;
	}
	
	//@Override
	public void paintComponent(Graphics page) {
		super.paintComponent(page);
		
		BufferedImage bufferImage = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D buffer = (Graphics2D) (bufferImage.getGraphics());
	
		_game._gui.drawGame(buffer);
	
		page.drawImage(bufferImage, 0, 0, null);
		_screen = bufferImage;
	}
}