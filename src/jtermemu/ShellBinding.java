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
					// boolean bracketed = textScr.isInBracketedPasteMode();
					boolean applic = textScr.hasApplicationCursorKeys();
					switch ( code ) {
					case KeyEvent.VK_UP: 
						if ( applic ) {
							bytes = "\u001bOA".getBytes();
						}
						else {
							bytes = "\u001b[A".getBytes();
						}
						break;
					case KeyEvent.VK_DOWN:
						if ( applic ) {
							bytes = "\u001bOB".getBytes();							
						}
						else {
							bytes = "\u001b[B".getBytes();
						}
						break;
					case KeyEvent.VK_RIGHT: 
						if ( applic ) {
							bytes = "\u001bOC".getBytes();
						}
						else {
							bytes = "\u001b[C".getBytes();
						}
						break;
					case KeyEvent.VK_LEFT: 
						if ( applic ) {
							bytes = "\u001bOD".getBytes();
						}
						else {
							bytes = "\u001b[D".getBytes();
						}
						break;
					case KeyEvent.VK_HOME:
						bytes = "\u001b[1~".getBytes();
						break;
					case KeyEvent.VK_END:
						bytes = "\u001b[4~".getBytes();
						break;
					case KeyEvent.VK_PAGE_UP:
						bytes = "\u001b[5~".getBytes();
						break;
					case KeyEvent.VK_PAGE_DOWN:
						bytes = "\u001b[6~".getBytes();
						break;
					case KeyEvent.VK_INSERT:
						bytes = "\u001b[2~".getBytes();
						break;
					case KeyEvent.VK_DELETE:
						bytes = "\u001b[3~".getBytes();
						break;
					case KeyEvent.VK_F1:
						bytes = "\u001b[11~".getBytes();
						break;
					case KeyEvent.VK_F2:
						bytes = "\u001b[12~".getBytes();
						break;
					case KeyEvent.VK_F3:
						bytes = "\u001b[13~".getBytes();
						break;
					case KeyEvent.VK_F4:
						bytes = "\u001b[14~".getBytes();
						break;
					case KeyEvent.VK_F5:
						bytes = "\u001b[15~".getBytes();
						break;
					case KeyEvent.VK_F6:
						bytes = "\u001b[17~".getBytes();
						break;
					case KeyEvent.VK_F7:
						bytes = "\u001b[18~".getBytes();
						break;
					case KeyEvent.VK_F8:
						bytes = "\u001b[19~".getBytes();
						break;
					case KeyEvent.VK_F9:
						bytes = "\u001b[20~".getBytes();
						break;
					case KeyEvent.VK_F10:
						bytes = "\u001b[21~".getBytes();
						break;
					case KeyEvent.VK_F11:
						bytes = "\u001b[23~".getBytes();
						break;
					case KeyEvent.VK_F12:
						bytes = "\u001b[24~".getBytes();
						break;
					case KeyEvent.VK_F13:
						bytes = "\u001b[25~".getBytes();
						break;
					case KeyEvent.VK_F14:
						bytes = "\u001b[26~".getBytes();
						break;
					case KeyEvent.VK_F15:
						bytes = "\u001b[28~".getBytes();
						break;
					case KeyEvent.VK_F16:
						bytes = "\u001b[29~".getBytes();
						break;
					case KeyEvent.VK_F17:
						bytes = "\u001b[31~".getBytes();
						break;
					case KeyEvent.VK_F18:
						bytes = "\u001b[32~".getBytes();
						break;
					case KeyEvent.VK_F19:
						bytes = "\u001b[33~".getBytes();
						break;
					case KeyEvent.VK_F20:
						bytes = "\u001b[34~".getBytes();
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
