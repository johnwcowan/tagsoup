// This file is part of TagSoup.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.  You may also distribute
// and/or modify it under version 2.0 of the Academic Free License.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// 
// 
// Model of document

package org.ccil.cowan.tagsoup;
import java.util.HashMap;

public class Schema {

	public static final int M_ANY = 0x7FFFFFFF;
	public static final int M_EMPTY = 0;

	public static final int M_1 = 1 << 1;
	public static final int M_2 = 1 << 2;
	public static final int M_3 = 1 << 3;
	public static final int M_4 = 1 << 4;
	public static final int M_5 = 1 << 5;
	public static final int M_6 = 1 << 6;
	public static final int M_7 = 1 << 7;
	public static final int M_8 = 1 << 8;
	public static final int M_9 = 1 << 9;
	public static final int M_10 = 1 << 10;
	public static final int M_11 = 1 << 11;
	public static final int M_12 = 1 << 12;
	public static final int M_13 = 1 << 13;
	public static final int M_14 = 1 << 14;
	public static final int M_15 = 1 << 15;
	public static final int M_16 = 1 << 16;
	public static final int M_17 = 1 << 17;
	public static final int M_18 = 1 << 18;
	public static final int M_19 = 1 << 19;
	public static final int M_20 = 1 << 20;
	public static final int M_21 = 1 << 21;
	public static final int M_22 = 1 << 22;
	public static final int M_23 = 1 << 23;
	public static final int M_24 = 1 << 24;
	public static final int M_25 = 1 << 25;
	public static final int M_26 = 1 << 26;
	public static final int M_27 = 1 << 27;
	public static final int M_28 = 1 << 28;
	public static final int M_29 = 1 << 29;
	public static final int M_30 = 1 << 30;

	public static final int F_RESTART = 1;
	public static final int F_CDATA = 2;

	private HashMap theEntities = 
		new HashMap();		// String -> Character
	private HashMap theElementTypes = 
		new HashMap();		// String -> ElementType

	public void elementType(String name, int model, int memberOf, int flags) {
		ElementType e = new ElementType(name, model, memberOf, flags, this);
		theElementTypes.put(name, e);
		}

	public void attribute(String elemName, String attrName,
				String type, String value) {
		ElementType e = getElementType(elemName);
		if (e == null) {
			throw new Error("Attribute " + attrName +
				" specified for unknown element type " +
				elemName);
			}
		e.setAttribute(attrName, type, value);
		}

	public void parent(String name, String parentName) {
		ElementType child = getElementType(name);
		ElementType parent = getElementType(parentName);
		if (child == null) {
			throw new Error("No child " + name + " for parent " + parentName);
			}
		if (parent == null) {
			throw new Error("No parent " + parentName + " for child " + name);
			}
		child.setParent(parent);
		}

	public void entity(String name, char value) {
		theEntities.put(name, new Character(value));
		}

	public ElementType getElementType(String name) {
		return (ElementType)(theElementTypes.get(name));
		}

	public char getEntity(String name) {
//		System.err.println("%% Looking up entity " + name);
		if (name.length() <= 1) return '&';
		if (name.charAt(1) == '#') {
			if (name.charAt(1) == 'x') {
				try {
					return (char)Integer.parseInt(name.substring(3), 16);
					}
				catch (NumberFormatException e) { return 0; }
				}
			try {
				return (char)Integer.parseInt(name.substring(2));
				}
			catch (NumberFormatException e) { return 0; }
			}
		Character c = (Character)theEntities.get(name);
		if (c == null) {
			return 0;
			}
		return c.charValue();
		}

	public String getURI() {
		return "";
		}

	public String getPrefix() {
		return "";
		}

	public static void main(String[] argv) {
		}
	}
