
package jtermemu;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JTermEmuMain {

	public static void createAndShowGUI() {
		
        JFrame frame = new JFrame("Terminal Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TerminalEmulator component = new TerminalEmulator();
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
