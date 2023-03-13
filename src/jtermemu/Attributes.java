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

public class Attributes {
	public static final int ATTRF_INVERSE 			   = 1 << 0;
	public static final int ATTRF_UNDERLINE 		   = 1 << 1;
	public static final int ATTRF_THIN 				   = 1 << 2;
	public static final int ATTRF_BOLD 				   = 1 << 3;
	public static final int ATTRF_BRIGHT 			   = 1 << 4;
	public static final int ATTRF_BLINKSLOW 		   = 1 << 5;
	public static final int ATTRF_BLINKFAST 		   = 1 << 6;
	public static final int ATTRF_OVERLINE 			   = 1 << 7;
	public static final int ATTRF_DOUBLE_UNDERLINE 	   = 1 << 8;
	public static final int ATTRF_STRIKE_DIAGONAL_BLTR = 1 << 9;
	public static final int ATTRF_STRIKE_DIAGONAL_TLBR = 1 << 10;
	public static final int ATTRF_STRIKE_VERTICAL 	   = 1 << 11;
	public static final int ATTRF_STRIKE_HORIZONTAL    = 1 << 12;
	public static final int ATTRF_BLACKEN 			   = 1 << 13;
}
