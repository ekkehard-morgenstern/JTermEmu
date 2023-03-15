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

    CONTACT INFO:
        E-Mail: ekkehard@ekkehardmorgenstern.de
        Mail: Ekkehard Morgenstern, Mozartstr. 1, D-76744 Woerth am Rhein, Germany, Europe 
*/

package jtermemu;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JTermEmuMain {

	public static void createAndShowGUI() {
		
        JFrame frame = new JFrame("Terminal Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TerminalEmulator component = new TerminalEmulator( frame );
        frame.add(component);     
        
        Dimension compSize = component.getMinimumSize();
        Dimension minSize = new Dimension( compSize.width + 16, compSize.height + 48 );
        
        frame.setMinimumSize( minSize );

        // display window
        frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

}
