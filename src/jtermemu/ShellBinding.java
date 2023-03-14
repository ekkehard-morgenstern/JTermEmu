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

public class ShellBinding {

	private Process shellProc = null;
	private InputThread stdOutCapture = null;
	private InputThread stdErrCapture = null;
	private TextScreen textScr = null;

	ShellBinding( TextScreen textScr_ ) {
		textScr = textScr_;
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
			stdOutCapture.start();
			stdErrCapture.start();
		}
		else {
			System.err.println( "SHELL environment variable undefined or empty" );
			System.exit( 1 );
		}
		
	}

	private void onShellExit() {
		System.out.println( "Shell exited" );
		shellProc = null;
	}
	
	public void periodicals() {
		// check shell process for output
		if ( shellProc == null ) return;		
	}
	
}
