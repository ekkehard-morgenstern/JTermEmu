package jtermemu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class TerminalEmulator extends Component {
	private static final long serialVersionUID = 1L;
	private Timer timer;
	private GraphicsScreen gfxScr = null;
	
	TerminalEmulator() {
		init();
	}
	
	private void init() {
		
		gfxScr = new GraphicsScreen();
		
		ActionListener timerListener = new ActionListener() {
		      public void actionPerformed( ActionEvent evt ) {
		          doTimer();
		      } 
		};
		
		timer = new Timer( 1000/60, timerListener );
		timer.start();		
	}
	
	private void doTimer() {
		// check if there's a size change
		Dimension size = getSize();
		gfxScr.update( size );
		// trigger redraw
		repaint();	
	}
	
	public void paint( Graphics g ) {
		gfxScr.paint( g, getSize() );
	}
	
	public Dimension getMinimumSize() {
		return gfxScr.getMinimumSize();
	}
	
}
