/*  JTermEmu - a terminal emulator written in Java
    Copyright (C) 2023  Ekkehard Morgenstern

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    NOTE: Programs created with PriamosBASIC do not fall under this license.

    CONTACT INFO:
        E-Mail: ekkehard@ekkehardmorgenstern.de
        Mail: Ekkehard Morgenstern, Mozartstr. 1, D-76744 Woerth am Rhein, Germany, Europe 
*/

package jtermemu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

public class TerminalEmulator extends Component {
	private static final long serialVersionUID = 1L;
	private Timer timer;
	private GraphicsScreen gfxScr = null;
	private ShellBinding shell = null;
	private JFrame frame = null;
	
	TerminalEmulator( JFrame frame_ ) {
		frame = frame_;
		init();
	}
	
	private void init() {
		
		gfxScr = new GraphicsScreen( frame );
		
		ActionListener timerListener = new ActionListener() {
		      public void actionPerformed( ActionEvent evt ) {
		          doTimer();
		      } 
		};
		
		timer = new Timer( 1000/60, timerListener );
		timer.start();
		
		shell = new ShellBinding( gfxScr.getTextScreen(), frame );
	}

	private void doTimer() {
		// check if there's a size change
		Dimension size = getSize();
		gfxScr.update( size );
		// trigger redraw
		repaint();
		// run shell periodicals
		shell.periodicals();
	}
	
	public void paint( Graphics g ) {
		gfxScr.paint( g, getSize() );
	}
	
	public Dimension getMinimumSize() {
		return gfxScr.getMinimumSize();
	}
	
}
