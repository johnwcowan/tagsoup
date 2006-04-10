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
/**
This class represents an element type in the schema.
An element type has a name, a content model vector, a member-of vector,
a flags vector, default attributes, and a schema to which it belongs.
@see Schema
*/

package org.ccil.cowan.tagsoup;
public class ElementType {

	private String theName;		// element type name
	private int theModel;		// bitmap: what the element contains
	private int theMemberOf;	// bitmap: what element is contained in
	private int theFlags;		// bitmap: element flags
	private AttributesImpl theAtts;	// default attributes
	private ElementType theParent;	// parent of this element type
	private Schema theSchema;	// schema to which this belongs

	/**
	Construct an ElementType:
	but it's better to use Schema.element() instead.
	The content model, member-of, and flags vectors are specified as ints.
	@param name The element type name
	@param model ORed-together bits representing the content models
	   allowed in the content of this element type
	@param memberOf ORed-together bits representing the content models
	   to which this element type belongs
	@param flags ORed-together bits representing the flags associated
	   with this element type
	@param schema The schema with which this element type will be
	associated
	*/

	public ElementType(String name, int model, int memberOf, int flags, Schema schema) {
		theName = name;
		theModel = model;
		theMemberOf = memberOf;
		theFlags = flags;
		theAtts = new AttributesImpl();
		theSchema = schema;
		}


	/**
	Returns the name of this element type.
	@return The name of the element type
	*/

	public String name() { return theName; }

	/**
	Returns the content models of this element type.
	@return The content models of this element type as a vector of bits
	*/

	public int model() { return theModel; }

	/**
	Returns the content models to which this element type belongs.
	@return The content models to which this element type belongs as a
	   vector of bits
	*/

	public int memberOf() { return theMemberOf; }

	/**
	Returns the flags associated with this element type.
	@return The flags associated with this element type as a vector of bits
	*/

	public int flags() { return theFlags; }

	/**
	Returns the default attributes associated with this element type.
	Attributes of type CDATA that don't have default values are
	typically not included.  Other attributes without default values
	have an internal value of <tt>null</tt>.
	The return value is an AttributesImpl to allow the caller to mutate
	the attributes.
	*/

	public AttributesImpl atts() {return theAtts;}

	/**
	Returns the parent element type of this element type.
	@return The parent element type
	*/

	public ElementType parent() {return theParent;}

	/**
	Returns the schema which this element type is associated with.
	@return The schema
	*/

	public Schema schema() {return theSchema;}


	/**
	Returns true if this element type can contain another element type.
	That is, if any of the models in this element's model vector
	match any of the models in the other element type's member-of
	vector.
	@param other The other element type
	*/

	public boolean canContain(ElementType other) {
		return (theModel & other.theMemberOf) != 0;
		}


	/**
	Sets an attribute and its value into an AttributesImpl object.
	@param atts The AttributesImpl object
	@param uri The namespace name of the attribute
	@param name The local name of the attribute
	@param type The type of the attribute
	@param value The value of the attribute
	*/

	public static void setAttribute(AttributesImpl atts, String uri, String name, String type, String value) {
		int i = atts.getIndex(name);
		if (i == -1) {
			name = name.intern();
			if (type == null) type = "CDATA";
			atts.addAttribute(uri, name, name, type, value);
			}
		else {
			if (type == null) type = atts.getType(i);
			atts.setAttribute(i, uri, name, name, type, value);
			}
		}

	/**
	Sets an attribute and its value into this element type.
	@param name The name of the attribute
	@param type The type of the attribute
	@param value The value of the attribute
	*/

	public void setAttribute(String name, String type, String value) {
		ElementType.setAttribute(theAtts, "", name, type, value);
		}


	/**
	Sets the parent element type of this element type.
	@param parent The parent element type
	*/

	public void setParent(ElementType parent) { theParent = parent; }

	}
