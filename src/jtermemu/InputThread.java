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

import java.io.IOException;
import java.io.InputStream;

public class InputThread extends Thread {

	private static final int 	BUFSIZ = 1024;
	
	private InputStream 		istream;
	private TextScreen 			textScr;
	private byte[] 				buffer;
	
	InputThread( InputStream istream_, TextScreen textScr_ ) {
		istream = istream_;
		textScr = textScr_;
		buffer  = new byte[BUFSIZ];
	}
	
	public void run() {
		try {
			for (;;) {
				int avail = istream.available();
				if ( avail > 0 ) {
					if ( avail > BUFSIZ ) avail = BUFSIZ;
					istream.read( buffer, 0, avail );
				}
				else {
					int b = istream.read();
					if ( b < 0 ) break;
					buffer[0] = (byte) b;
					avail = 1;
				}
				textScr.write( buffer, 0, avail );
			}
		} 
		catch ( IOException e ) {
			System.err.println( "Exception while reading standard output from shell:" );
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
}
