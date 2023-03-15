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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;

/**
 * 
 * Character cell modes:
 * 
 * Attribute bits:
 * 		0 - inverse
 * 		1 - underline
 * 		2 - thin
 * 		3 - bold
 * 		4 - bright / intense
 * 		5 - blinking slow
 * 		6 - blinking fast
 *      7 - overline
 *      8 - double underline
 *      9 - strike out diagonally BL-TR
 *     10 - strike out diagonally TL-BR
 *     11 - strike out vertically
 *     12 - strike out horizontally
 *     13 - blackened
 *      
 * Character cell layout:
 * 
 * 		11 x 15 pixels (of which 8 x 8 pixels is the character glyph)
 * 
 * 				ooooooooooo			o = overline
 * 				-----------
 *				........rb-			b = bold    3x smear
 *				........rb-			. = character glyph cell / thin
 *				........rb-			r = regular 2x smear
 *				........rb-
 *				........rb-
 *				........rb-
 *				........rb-
 *				........rb-
 *				-----------
 *				uuuuuuuuuuu			u = underline
 *				-----------
 *				ddddddddddd			d = double underline
 *				-----------
 * 
 * @author Ekkehard Morgenstern
 *
 */
public class GraphicsScreen {
	private BufferedImage image = null;
	private TextScreen textScr = null;
	private Dimension minSize = null;
	private long frameCounter = 0;
	private JFrame frame = null;
	
	private static final int CELL_WIDTH = 11;
	private static final int CELL_HEIGHT = 15;
	
	public GraphicsScreen( JFrame frame_ ) {
		frame = frame_;
		init();
	}
	
	private void init() {
		textScr = new TextScreen( frame );
		initImage();
	}
	
	private void initImage() {
		int cols = textScr.getColumns();
		int rows = textScr.getRows();
		int allocWidth  = cols * CELL_WIDTH;
		int allocHeight = rows * CELL_HEIGHT;
		image = new BufferedImage( allocWidth, allocHeight, BufferedImage.TYPE_BYTE_INDEXED,
					new IndexColorModel( 4, 16, 
							new byte[] { (byte) 0x88, (byte) 0x00, (byte) 0x88, (byte) 0x00, 
										 (byte) 0x88, (byte) 0x00, (byte) 0x88, (byte) 0x00, 
										 (byte) 0xff, (byte) 0xaa, (byte) 0xff, (byte) 0x00, 
										 (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00 }, 
							new byte[] { (byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x88, 
										 (byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x88, 
										 (byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0xff, 
										 (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff },
							new byte[] { (byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
									     (byte) 0x00, (byte) 0x88, (byte) 0x88, (byte) 0x88, 
									     (byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0x00, 
									     (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff } 
					)
				);		
		minSize = new Dimension( allocWidth, allocHeight );
	}
	
	private void updateImage() {
		if ( image == null ) return;
		
		if ( ++frameCounter < 0 ) {
			frameCounter = 0;
		}
		
		int cols = textScr.getColumns();
		int rows = textScr.getRows();
		boolean blinkSlow = false;
		boolean blinkFast = false;
		if ( ( frameCounter % 60 ) >= 30 ) {
			blinkSlow = true;
		}
		if ( ( frameCounter % 30 ) >= 15 ) {
			blinkFast = true;
		}
		int[] charBuf = new int [ CELL_WIDTH * CELL_HEIGHT ];
		int[] buffer  = textScr.getBuffer();
		int[] data    = new int [ 8 ];
		WritableRaster raster = image.getRaster();
		for ( int y=0; y < rows; ++y ) {
			for ( int x=0; x < cols; ++x ) {
				int cell  = buffer[ y * cols + x ];
				int chr   =   cell 							   & 255;
				int bgcol = ( cell >> TextScreen.BGCOL_SHIFT ) & 15;
				int fgcol = ( cell >> TextScreen.FGCOL_SHIFT ) & 15;
				int attr  = ( cell >> TextScreen.ATTR_SHIFT  ) & 32767;
				int shift = ( attr & Attributes.ATTRF_THIN ) != 0 ? 1 : 2;
				if ( ( attr & Attributes.ATTRF_BLACKEN ) == 0 ) {
					if ( ( attr & Attributes.ATTRF_BRIGHT ) != 0 && fgcol < 8 ) {
						fgcol += 8;
					}
					if ( ( attr & Attributes.ATTRF_INVERSE ) != 0 || 
						 ( ( attr & Attributes.ATTRF_BLINKFAST ) != 0 && blinkFast ) ||
						 ( ( attr & Attributes.ATTRF_BLINKSLOW ) != 0 && blinkSlow ) ) {
						int temp = bgcol; bgcol = fgcol; fgcol = temp;
					}					
				}
				else {
					bgcol = fgcol = 1;
				}
				if ( chr >= FontData.LOW_CHAR && chr <= FontData.HIGH_CHAR ) {
					int offs = ( chr - FontData.LOW_CHAR ) * 8;
					for ( int cy=0; cy < 8; ++cy ) {
						int b = ( (int) FontData.bits[ offs + cy ] ) & 255;
						data[cy] = b << shift;
					}
				}
				else {
					for ( int cy=0; cy < 8; ++cy ) {
						int b = ( (int) FontData.undefbits[ cy ] ) & 255;
						data[cy] = b << shift;
					}					
				}
				if ( ( attr & Attributes.ATTRF_BOLD ) != 0 ) {
					for ( int cy=0; cy < 8; ++cy ) {
						data[cy] |= ( data[cy] >> 1 ) | ( data[cy] >> 2 );
					}
				}
				else if ( ( attr & Attributes.ATTRF_THIN ) == 0 ) {
					for ( int cy=0; cy < 8; ++cy ) {
						data[cy] |= data[cy] >> 1;
					}
				}
				for ( int n=0; n < CELL_WIDTH * CELL_HEIGHT; ++n ) {
					charBuf[n] = bgcol;
				}
				for ( int cy=0; cy < 8; ++cy ) {
					int b = data[cy];
					for ( int cx=0; cx < 10; ++cx ) {
						int b2  = 1 << ( 9 - cx );
						int col = ( b & b2 ) != 0 ? fgcol : bgcol;
						charBuf[ ( 2 + cy ) * CELL_WIDTH + cx ] = col;
					}
				}
				if ( ( attr & Attributes.ATTRF_OVERLINE ) != 0 ) {
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ cx ] = fgcol;
					}					
				}
				if ( ( attr & Attributes.ATTRF_DOUBLE_UNDERLINE ) != 0 ) {
					int offs1 = ( CELL_HEIGHT-3 ) * CELL_WIDTH;
					int offs2 = ( CELL_HEIGHT-1 ) * CELL_WIDTH;
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs1 + cx ] = fgcol;
					}
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs2 + cx ] = fgcol;
					}
				}
				else if ( ( attr & Attributes.ATTRF_UNDERLINE ) != 0 ) {
					int offs1 = ( CELL_HEIGHT-3 ) * CELL_WIDTH;
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs1 + cx ] = fgcol;
					}					
				}
				if ( ( attr & Attributes.ATTRF_STRIKE_DIAGONAL_TLBR ) != 0 ) {
					int offs = 2 * CELL_WIDTH;
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs + cx*CELL_WIDTH + cx ] = 1;
					}					
				}
				if ( ( attr & Attributes.ATTRF_STRIKE_DIAGONAL_BLTR ) != 0 ) {
					int offs = 2 * CELL_WIDTH;
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs + cx*CELL_WIDTH + (CELL_WIDTH-1-cx) ] = 1;
					}										
				}
				if ( ( attr & Attributes.ATTRF_STRIKE_HORIZONTAL ) != 0 ) {
					int offs = ( 2 + 5 ) * CELL_WIDTH;
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs + cx ] = 1;
					}																				
				}
				if ( ( attr & Attributes.ATTRF_STRIKE_VERTICAL ) != 0 ) {
					int offs = 2 * CELL_WIDTH;
					for ( int cx=0; cx < CELL_WIDTH; ++cx ) {
						charBuf[ offs + cx*CELL_WIDTH + CELL_WIDTH/2 ] = 1;
					}																				
				}
				raster.setPixels( x*CELL_WIDTH, y*CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT, charBuf );
			}
		}
	}
	
	public void update( Dimension size ) {
		updateImage();
	}
	
	public void paint( Graphics g, Dimension paintSize ) {
		if ( image == null ) return;
		
		int scaleX = 1;
		int scaleY = 1;
		
		int imageW = image.getWidth();
		int imageH = image.getHeight(); 
		
		while ( imageW * scaleX < paintSize.width ) {
			int newScaleX = scaleX + 1;
			if ( imageW * newScaleX <= paintSize.width ) {
				scaleX = newScaleX;
				continue;
			}
			else {
				break;
			}
		}
		
		while ( imageH * scaleY < paintSize.height ) {
			int newScaleY = scaleY + 1;
			if ( imageH * newScaleY <= paintSize.height ) {
				scaleY = newScaleY;
				continue;
			}
			else {
				break;
			}
		}
		
		int paintW = imageW * scaleX;
		int paintH = imageH * scaleY;
		
		int left   = ( paintSize.width  - paintW ) / 2;
		int top    = ( paintSize.height - paintH ) / 2;
		
		if ( top > 0 ) {
			g.clearRect( 0,  0, paintSize.width, top );
			g.clearRect( 0,  top + paintH, paintSize.width, paintSize.height - paintH - top );
		}
		if ( left > 0 ) {
			g.clearRect( 0, top, left, paintH );
			g.clearRect( left + paintW, top, paintSize.width - paintW - left, paintH );
		}
		
		g.drawImage( image, left, top, paintW, paintH, null );
	}
	
	public Dimension getMinimumSize() {
		return minSize;
	}
	
	public TextScreen getTextScreen() {
		return textScr;
	}
	

}
