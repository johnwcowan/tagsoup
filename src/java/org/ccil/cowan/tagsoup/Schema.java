// This file is part of TagSoup.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.  You may also distribute
// and/or modify it under version 2.1 of the Academic Free License.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// 
// 
// Model of document

package org.ccil.cowan.tagsoup;
import java.util.HashMap;

/**
Abstract class representing a TSSL schema.
Actual TSSL schemas are compiled into concrete subclasses of this class.
**/

public abstract class Schema {

	public static final int M_ANY = 0xFFFFFFFF;
	public static final int M_EMPTY = 0;
	public static final int M_PCDATA = 1 << 30;
	public static final int M_ROOT = 1 << 31;


	public static final int F_RESTART = 1;
	public static final int F_CDATA = 2;

	private HashMap theEntities = 
		new HashMap();		// String -> Character
	private HashMap theElementTypes = 
		new HashMap();		// String -> ElementType

	/**
	Add or replace an element type for this schema.
	@param name Name (Qname) of the element
	@param model Models of the element's content as a vector of bits
	@param memberOf Models the element is a member of as a vector of bits
	@param flags Flags for the element
	**/

	public void elementType(String name, int model, int memberOf, int flags) {
		ElementType e = new ElementType(name, model, memberOf, flags, this);
		theElementTypes.put(name, e);
		}

	/**
	Add or replace a default attribute for an element type in this schema.
	@param elemName Name (Qname) of the element type
	@param attrName Name (Qname) of the attribute
	@param type Type of the attribute
	@param value Default value of the attribute; null if no default
	**/

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

	/**
	Specify natural parent of an element in this schema.
	@param name Name of the child element
	@param parentName Name of the parent element
	**/

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

	/**
	Add to or replace a character entity in this schema.
	@param name Name of the entity
	@param value Value of the entity
	**/

	public void entity(String name, char value) {
		theEntities.put(name, new Character(value));
		}

	/**
	Get an ElementType by name.
	@param name Name (Qname) of the element type
	@return The corresponding ElementType
	**/

	public ElementType getElementType(String name) {
		return (ElementType)(theElementTypes.get(name));
		}

	/**
	Get an entity value by name.
	@param name Name of the entity
	@return The corresponding character, or 0 if none
	**/

	public char getEntity(String name) {
//		System.err.println("%% Looking up entity " + name);
		if (name.length() == 0) return 0;
		if (name.charAt(0) == '#') {
			if (name.charAt(1) == 'x') {
				try {
					return (char)Integer.parseInt(name.substring(2), 16);
					}
				catch (NumberFormatException e) { return 0; }
				}
			try {
				return (char)Integer.parseInt(name.substring(1));
				}
			catch (NumberFormatException e) { return 0; }
			}
		Character c = (Character)theEntities.get(name);
		if (c == null) {
			return 0;
			}
		return c.charValue();
		}

	/**
	Return the URI (namespace name) of this schema.
	**/

	public String getURI() {
		return "";
		}

	/**
	Return the default prefix of this schema.
	**/

	public String getPrefix() {
		return "";
		}

	}
