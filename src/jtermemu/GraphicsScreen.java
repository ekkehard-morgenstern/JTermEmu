package jtermemu;

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
 * 		10 x 15 pixels (of which 8 x 8 pixels is the character glyph)
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
	private Dimension oldsize = null;
	private Dimension currsize = null;
	private BufferedImage image = null;
	
	private final int ATTRF_INVERSE 			 = 1 << 0;
	private final int ATTRF_UNDERLINE 			 = 1 << 1;
	private final int ATTRF_THIN 				 = 1 << 2;
	private final int ATTRF_BOLD 				 = 1 << 3;
	private final int ATTRF_BRIGHT 				 = 1 << 4;
	private final int ATTRF_BLINKSLOW 			 = 1 << 5;
	private final int ATTRF_BLINKFAST 		 	 = 1 << 6;
	private final int ATTRF_OVERLINE 			 = 1 << 7;
	private final int ATTRF_DOUBLE_UNDERLINE 	 = 1 << 8;
	private final int ATTRF_STRIKE_DIAGONAL_BLTR = 1 << 9;
	private final int ATTRF_STRIKE_DIAGONAL_TLBR = 1 << 10;
	private final int ATTRF_STRIKE_VERTICAL 	 = 1 << 11;
	private final int ATTRF_STRIKE_HORIZONTAL 	 = 1 << 12;
	private final int ATTRF_BLACKEN 			 = 1 << 13;
	
	static final int[] dummyChar = {
			0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,
			0,0,0,1,1,0,0,0,
			0,0,1,0,0,1,0,0,
			0,1,0,0,0,0,1,0,
			0,1,0,0,0,0,1,0,
			0,1,1,1,1,1,1,0,
			0,1,0,0,0,0,1,0,
			0,1,0,0,0,0,1,0,
			0,1,0,0,0,0,1,0,
			0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0			
	};
	
	private void initImage() {
		Dimension size = currsize;
		image = new BufferedImage( size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED,
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
	}
	
	private void updateImage() {
		if ( image == null ) return;
		
		int width  = image.getWidth();
		int height = image.getHeight();
		if ( width <= 0 || height <= 0 ) return;
		
		int rows = height / 12;
		int cols = width  / 8;

		WritableRaster raster = image.getRaster();
		for ( int y=0; y < rows; ++y ) {
			for ( int x=0; x < cols; ++x ) {
				raster.setPixels( x*8,  y*12,  8,  12, dummyChar );
			}
		}
	}
	
	public void update( Dimension size ) {
		currsize = size;
		if ( oldsize == null || currsize.width != oldsize.width || currsize.height != oldsize.height ) {
			oldsize = currsize;
			initImage();
		}
		updateImage();
	}
	
	public void paint( Graphics g ) {
		if ( image == null ) return;
		g.drawImage( image, 0, 0, null );
	}
	

}
