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
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

public class OutputThread extends Thread {
	
	private static final int 	BUFSIZ = 128;
	
	private OutputStream 		ostream;
	private byte[] 				buffer;
	private int 				bufFill;
	private Semaphore 			bufSem;
	
	OutputThread( OutputStream ostream_ ) {
		ostream = ostream_;
		buffer  = new byte [BUFSIZ];
		bufFill = 0;
		bufSem  = new Semaphore( 1, true );
	}
	
	public void enterInput( byte[] bytes ) {
		try {
			bufSem.acquire();
		}
		catch ( InterruptedException e ) {
			System.err.println( "Thread was interrupted while waiting for semaphore" );
			e.printStackTrace();
			System.exit( 1 );
		}
		if ( bufFill + bytes.length < buffer.length ) {
			for ( int i=0; i < bytes.length; ++i ) {
				buffer[bufFill++] = bytes[i];
			}
		}
		bufSem.release();
	}
	
	public void run() {
		byte[]  input = new byte [BUFSIZ];
		int 	fill  = 0;
		try {
			for (;;) {
				bufSem.acquire();
				fill    = bufFill;
				bufFill = 0;
				for ( int i=0; i < fill; ++i ) {
					input[i] = buffer[i];
				}
				bufSem.release();
				if ( fill > 0 ) {
					ostream.write( input, 0, fill );
					ostream.flush();
				}
				else {
					sleep( 20 );	// 1/50 sec = 1000/50 msec
				}
			}
		} 
		catch ( InterruptedException e ) {
			System.err.println( "Thread was interrupted while waiting for semaphore" );
			e.printStackTrace();
			System.exit( 1 );
		}
		catch ( IOException e ) {
			System.err.println( "Exception while writing standard input from shell:" );
			e.printStackTrace();
			System.exit(1);
		}
	}
	

}
