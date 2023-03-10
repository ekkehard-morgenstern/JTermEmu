package jtermemu;

public class TextScreen {

	private int width = 80;
	private int height = 25;
	
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
	
	TextScreen() {
		init();
	}
	
	private void init() {
		buffer = new int [ width * height ];
		int a = 0;
		int fgcol = 1;
		int bgcol = 0;
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
				buffer[o+x] = ( attr << ATTR_SHIFT ) | ( bgcol << BGCOL_SHIFT ) | ( fgcol << FGCOL_SHIFT ) | 0x23;
				if ( ++fgcol > 7 ) {
					fgcol = 0;
					if ( ++bgcol > 7 ) {
						bgcol = 0;
					}
				}
			}
		}
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
