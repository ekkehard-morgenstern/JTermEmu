
package jtermemu;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JTermEmuMain {

	public static void createAndShowGUI() {
		
        JFrame frame = new JFrame("Terminal Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds( 0, 0, 800, 600 );
        
        TerminalEmulator component = new TerminalEmulator();
        frame.add(component);

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
