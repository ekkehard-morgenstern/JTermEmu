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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JFrame;

public class ShellBinding {

	private Process shellProc = null;
	private InputThread stdOutCapture = null;
	private InputThread stdErrCapture = null;
	private OutputThread stdInFeeder = null;
	private TextScreen textScr = null;
	private JFrame frame = null;

	ShellBinding( TextScreen textScr_, JFrame frame_ ) {
		textScr = textScr_;
		frame = frame_;
		init();
	}
	
	private void init() {

		String shellPath;
		if ( System.getenv().containsKey( "SHELL" ) ) {
			shellPath = System.getenv().get( "SHELL" );
		}
		else {
			shellPath = "";
		}
		if ( !shellPath.isEmpty() ) {
			try {
				ProcessBuilder pb = new ProcessBuilder( shellPath, "-i" );
				pb.environment().put( "TERM", "xterm-256color" );
				shellProc = pb.start();
				shellProc.onExit().thenAccept( x -> onShellExit() );
			}
			catch( Exception e ) {
				System.err.println( "Exception while starting shell:" );
				e.printStackTrace();
				System.exit( 1 );
			}
			stdOutCapture = new InputThread( shellProc.getInputStream(), textScr );
			stdErrCapture = new InputThread( shellProc.getErrorStream(), textScr );
			stdInFeeder   = new OutputThread( shellProc.getOutputStream() );

			stdOutCapture.start();
			stdErrCapture.start();
			stdInFeeder.start();
			
			System.out.printf( "pid = %d\n", (int) shellProc.pid() );
			
			KeyListener keyListener = new KeyListener() {
				public void keyTyped( KeyEvent e ) {
					char c = e.getKeyChar();
					if ( c == KeyEvent.CHAR_UNDEFINED ) {
						return;
					}
					byte[] bytes;
					int v = (int) c;
					if ( v < 0x80 ) {
						bytes = new byte [1];
						bytes[0] = (byte) c;
					}
					else if ( v < 0x800 ) {
						bytes = new byte [2];
						bytes[0] = (byte)( 0xc0 | ( c >> 6 ) );
						bytes[1] = (byte)( 0x80 | ( c & 63 ) );
					}
					else if ( v < 0x10000 ) {
						bytes = new byte [3];
						bytes[0] = (byte)( 0xe0 | ( c >> 12 ) );
						bytes[1] = (byte)( 0x80 | ( ( c >> 6 ) & 63 ) );
						bytes[2] = (byte)( 0x80 | ( c & 63 ) );
					}
					else if ( v < 0x200000 ) {
						bytes = new byte [4];
						bytes[0] = (byte)( 0xf0 | ( c >> 18 ) );
						bytes[1] = (byte)( 0x80 | ( ( c >> 12 ) & 63 ) );
						bytes[2] = (byte)( 0x80 | ( ( c >> 6 ) & 63 ) );
						bytes[3] = (byte)( 0x80 | ( c & 63 ) );					
					}
					else {
						return;
					}
					stdInFeeder.enterInput( bytes );
				}
				public void keyPressed( KeyEvent e ) {
					char c = e.getKeyChar();
					if ( c != KeyEvent.CHAR_UNDEFINED ) return;
					int code = e.getKeyCode();
					// System.out.printf( "keyCode = %d\n", code );
					byte[] bytes = null;
					switch ( code ) {
					case KeyEvent.VK_UP: 
						bytes = "\u001b[A".getBytes();
						break;
					case KeyEvent.VK_DOWN: 
						bytes = "\u001b[B".getBytes();
						break;
					case KeyEvent.VK_RIGHT: 
						bytes = "\u001b[C".getBytes();
						break;
					case KeyEvent.VK_LEFT: 
						bytes = "\u001b[D".getBytes();
						break;
					}
					if ( bytes != null ) {
						stdInFeeder.enterInput( bytes );
					}
				}
				public void keyReleased( KeyEvent e ) {
					
				}
			};
			
			frame.addKeyListener( keyListener );
		}
		else {
			System.err.println( "SHELL environment variable undefined or empty" );
			System.exit( 1 );
		}
		
	}

	private void onShellExit() {
		System.out.println( "Shell exited" );
		shellProc = null;
		System.exit( 0 );
	}
	
	public void periodicals() {
		// check shell process for output
		if ( shellProc == null ) return;		
	}
	
}
