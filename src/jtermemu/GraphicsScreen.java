package jtermemu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

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
	
	private static final int CELL_WIDTH = 11;
	private static final int CELL_HEIGHT = 15;
	
	public GraphicsScreen() {
		init();
	}
	
	private void init() {
		textScr = new TextScreen();
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
				for ( int cy=0; cy < 8; ++cy ) {
					int b = data[cy];
					for ( int cx=0; cx < 10; ++cx ) {
						int b2  = 1 << ( 9 - cx );
						int col = ( b & b2 ) != 0 ? fgcol : bgcol;
						charBuf[ ( 2 + cy ) * CELL_WIDTH + cx ] = col;
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
	

}
