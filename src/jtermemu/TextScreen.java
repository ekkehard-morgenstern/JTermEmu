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

import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

public class TextScreen {

	private int width = 80;
	private int height = 25;
	private int cursX = 0;
	private int cursY = 0;
	private int colorF = 0, colorB = 0;
	private int userA = 0;
	private boolean escape = false;
	private boolean osc = false;
	private boolean csi = false;
	private boolean utf8 = false;
	private int utf8_remain = 0;
	private int utf8_cp = 0;
	private String oscSeq, csiSeq;
	private JFrame frame;
	private Semaphore writeSem = null;
	private int cursorVisibleNest = 0;
	
	public static final int FGCOL_SHIFT = 8;
	public static final int BGCOL_SHIFT = 12;
	public static final int ATTR_SHIFT  = 16;
	
	/**
	 * Text screen buffer.
	 * Each cell contains the following fields:
	 * 
	 * 	<attr.15> <bgCol.4> <fgCol.4> <char.8>
	 */
	private int[] buffer = null; 
	
	TextScreen( JFrame frame_ ) {
		frame = frame_;
		init();
	}
	
	/*
	private void test() {
		int a = 0;
		int fgcol = 1;
		int bgcol = 0;
		int demomin = 0x21;
		int demomax = 0x7f;
		int chr = demomin;
		for ( int y=0; y < height; ++y ) {
			int o = y * width;
			for ( int x=0; x < width; ++x ) {
				int attr = 0;
				if ( ++a >= 15 ) a = 0;
				switch ( a ) {
				case 0:	attr |= Attributes.ATTRF_THIN; break;
				case 1: break;
				case 2: attr |= Attributes.ATTRF_BOLD; break;
				case 3: attr |= Attributes.ATTRF_BRIGHT; break;
				case 4: attr |= Attributes.ATTRF_INVERSE; break;
				case 5: attr |= Attributes.ATTRF_BLINKFAST; break;
				case 6: attr |= Attributes.ATTRF_BLINKSLOW; break;
				case 7: attr |= Attributes.ATTRF_BLACKEN; break;
				case 8: attr |= Attributes.ATTRF_UNDERLINE; break;
				case 9: attr |= Attributes.ATTRF_DOUBLE_UNDERLINE; break;
				case 10: attr |= Attributes.ATTRF_OVERLINE; break;
				case 11: attr |= Attributes.ATTRF_STRIKE_DIAGONAL_BLTR; break;
				case 12: attr |= Attributes.ATTRF_STRIKE_DIAGONAL_TLBR; break;
				case 13: attr |= Attributes.ATTRF_STRIKE_HORIZONTAL; break;
				case 14: attr |= Attributes.ATTRF_STRIKE_VERTICAL; break;
				}
				buffer[o+x] = ( attr << ATTR_SHIFT ) | ( bgcol << BGCOL_SHIFT ) | ( fgcol << FGCOL_SHIFT ) | chr;
				if ( ++fgcol > 7 ) {
					fgcol = 0;
					if ( ++bgcol > 7 ) {
						bgcol = 0;
					}
				}
				if ( ++chr > demomax ) {
					chr = demomin;
				}
			}
		}		
	}
	*/
	
	private void init() {
		buffer = new int [ width * height ];
		oscSeq = new String();
		csiSeq = new String();
		writeSem = new Semaphore( 1, true );
		cls( 1, 0 );
	}
	
	private void color( int fgcol, int bgcol ) {
		colorF = fgcol & 15;
		colorB = bgcol & 15;
	}
	
	private void attrib( int a ) {
		userA = a & 32767;
	}
	
	private void cls( int fgcol, int bgcol ) {
		color( fgcol, bgcol ); attrib( 0 );
		int v = ( userA << ATTR_SHIFT ) | ( colorB << BGCOL_SHIFT ) | ( colorF << FGCOL_SHIFT ) | 0x20;
		int bufsiz = width * height;
		for ( int i=0; i < bufsiz; ++i ) buffer[i] = v;
		showCursor();
	}
	
	private void gotoxy( int x, int y ) {
		hideCursor();
		cursX = x;
		cursY = y;
		if ( cursX < 0 ) cursX = 0; else if ( cursX >= width  ) cursX = width -1;
		if ( cursY < 0 ) cursY = 0; else if ( cursY >= height ) cursY = height-1;
		showCursor();
	}
	
	private void handleOsc() {
		System.out.println( oscSeq );
		int pos = oscSeq.indexOf( ';' );
		if ( pos < 0 ) return;
		String s1 = oscSeq.substring( 0, pos );
		String s2 = oscSeq.substring( pos + 1 );
		// System.out.println( s1 );
		// System.out.println( s2 );
		if ( s1.equals("0") ) {
			// Set Window Title + Icon
			frame.setTitle( s2 );			
		}
	}
	
	private int colorXlat( int isoNum ) {
		switch ( isoNum ) {
		case 0:	// BLACK
			return 1;
		case 1:	// RED
			return 2;
		case 2:	// GREEN
			return 3;
		case 3:	// YELLOW
			return 4;
		case 4: // BLUE
			return 5;
		case 5:	// MAGENTA
			return 6;
		case 6:	// CYAN
			return 7;
		case 7:	// WHITE
			return 0;
		}
		return 0;
	}
	private void handleCsi( int c ) {
		// System.out.println( csiSeq );
		// System.out.println( (char) c );
		int[] args = new int [10];
		int nargs = 0;
		int oldpos = 0;
		int len = csiSeq.length();
		while ( oldpos < len ) {
			int pos = csiSeq.indexOf( ';', oldpos );
			if ( pos < 0 ) pos = len;
			args[nargs++] = Integer.valueOf( csiSeq.substring( oldpos, pos ) ).intValue();
			oldpos = pos + 1;
		}
		if ( c == 'm' ) {
			for ( int i=0; i < nargs; ++i ) {
				int arg = args[i];
				if ( arg >= 30 && arg <= 37 ) {
					colorF = colorXlat( arg - 30 );
				}
				else if ( arg >= 40 && arg <= 47 ) {
					colorB = colorXlat( arg - 40 );					
				}
				else if ( arg >= 90 && arg <= 97 ) {
					colorF = colorXlat( arg - 90 );					
				}
				else if ( arg >= 100 && arg <= 107 ) {
					colorB = colorXlat( arg - 100 ) + 8;
				}
				else {
					switch ( arg ) {
					case 0:	// NORMAL
						userA = 0;
						break;
					case 1: // BOLD / INTENSE
						userA |= Attributes.ATTRF_BOLD;
						break;
					case 4:	// UNDERLINED
						userA |= Attributes.ATTRF_UNDERLINE;
						break;
					case 5:	// BLINK
						userA |= Attributes.ATTRF_BLINKSLOW;
						break;
					case 7:	// INVERSE
						userA |= Attributes.ATTRF_INVERSE;
						break;
					case 8:	// HIDDEN
						userA |= Attributes.ATTRF_BLACKEN;
						break;
					case 22: 	// not BOLD / INTENSE
						userA &= ~Attributes.ATTRF_BOLD;
						break;
					case 24:	// not UNDERLINED
						userA &= ~Attributes.ATTRF_UNDERLINE;
						break;
					case 25:	// not BLINK
						userA &= ~Attributes.ATTRF_BLINKSLOW;
						break;
					case 27:	// not INVERSE
						userA &= ~Attributes.ATTRF_INVERSE;
						break;
					case 28:	// not HIDDEN
						userA &= ~Attributes.ATTRF_BLACKEN;
						break;
					}
				}
			}
		}
	}
	
	private void scrollUp() {
		
	}
	
	private void writech( int c, boolean iscp ) {
		if ( utf8 ) {
			if ( ( c & 0xc0 ) == 0x80 ) {
				utf8_cp = ( utf8_cp << 6 ) | ( c & 0x3f );
				if ( --utf8_remain <= 0 ) {
					int cp = utf8_cp;
					utf8 = false; utf8_cp = 0; utf8_remain = 0;
					writech( cp, true );
				}
			}
			else {
				int cp = utf8_cp;
				utf8 = false; utf8_cp = 0; utf8_remain = 0;
				writech( cp, true  ); 
				writech( c , false );
			}
			return;
		}
		else if ( !iscp ) {
			if ( ( c & 0xe0 ) == 0xc0 ) {	// 2 byte cp
				utf8 = true; utf8_cp = c & 0x1f; utf8_remain = 1;
				return;
			}
			else if ( ( c & 0xf0 ) == 0xe0 ) { 	// 3 byte cp
				utf8 = true; utf8_cp = c & 0x0f; utf8_remain = 2;
				return;
			}
			else if ( ( c & 0xf8 ) == 0xf0 ) {	// 4 byte cp
				utf8 = true; utf8_cp = c & 0x07; utf8_remain = 3;				
				return;
			}
		}
		if ( escape ) {
			if ( !osc && !csi ) {
				if ( c == ']' ) {	// OSC
					osc = true;
				}
				else if ( c == '[' ) {	// CSI
					csi = true;
				}
			}
			else if ( osc ) {
				if ( c == 7 ) {
					handleOsc();
					oscSeq = new String();
					osc = false;
					escape = false;
				}	
				else {
					oscSeq += (char) c;
				}
			}
			else if ( csi ) {
				if ( ( c < '0' || c > '9' ) && c != ';' ) {
					handleCsi( c );
					csiSeq = new String();
					csi = false;
					escape = false;
				}
				else {
					csiSeq += (char) c;
				}
			}
		} 
		else if ( c == 27 ) {
			escape = true;
		}
		else if ( c == 13 ) {
			hideCursor();
			cursX = 0;
			showCursor();
		}
		else if ( c == 10 ) {
			hideCursor();
			cursX = 0;
			if ( ++cursY >= height ) {
				cursY = height - 1;
				scrollUp();
			}
			showCursor();
		}
		else {
			hideCursor();
			int v = ( userA << ATTR_SHIFT ) | ( colorB << BGCOL_SHIFT ) | ( colorF << FGCOL_SHIFT ) | c;
			buffer[ cursY * width + cursX ] = v;
			if ( ++cursX >= width ) {
				cursX = 0;
				if ( ++cursY >= height ) {
					cursY = height - 1;
					scrollUp();
				}
			}
			showCursor();
		}
	}
	
	private void hideCursor() {
		if ( --cursorVisibleNest == 0 ) {
			buffer[ cursY * width + cursX ] &= ~( Attributes.ATTRF_BLINKSLOW << ATTR_SHIFT );
		}
	}
	
	private void showCursor() {
		if ( ++cursorVisibleNest == 1 ) {
			buffer[ cursY * width + cursX ] |= Attributes.ATTRF_BLINKSLOW << ATTR_SHIFT;			
		}
	}
	
	public void write( byte[] arr, int offs, int len ) {
		try {
			writeSem.acquire();
		}
		catch ( InterruptedException e ) {
			System.err.println( "Thread was interrupted while waiting for semaphore" );
			e.printStackTrace();
			System.exit( 1 );
		}
		hideCursor();
		for ( int i=0; i < len; ++i ) {
			int c = arr[offs+i] & 255;
			writech( c, false );			
		}
		showCursor();
		writeSem.release();
	}
	
	public int[] getBuffer() {
		return buffer;
	}
	
	public int getColumns() {
		return width;
	}
	
	public int getRows() {
		return height;
	}
	
}
