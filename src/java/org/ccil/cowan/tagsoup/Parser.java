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
// The TagSoup parser

package org.ccil.cowan.tagsoup;
import java.util.HashMap;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.LexicalHandler;


/**
The SAX parser class.
**/
public class Parser extends DefaultHandler implements ScanHandler, XMLReader, LexicalHandler {

	// XMLReader implementation

	private ContentHandler theContentHandler = this;
	private LexicalHandler theLexicalHandler = this;
	private DTDHandler theDTDHandler = this;
	private ErrorHandler theErrorHandler = this;
	private EntityResolver theEntityResolver = this;
	private Schema theSchema;
	private Scanner theScanner;
	private AutoDetector theAutoDetector;

	public final static String namespacesFeature =
		"http://xml.org/sax/features/namespaces";
	public final static String namespacePrefixesFeature =
		"http://xml.org/sax/features/namespace-prefixes";
	public final static String externalGeneralEntitiesFeature =
		"http://xml.org/sax/features/external-general-entities";
	public final static String externalParameterEntitiesFeature =
		"http://xml.org/sax/features/external-parameter-entities";
	public final static String ignoreBogonsFeature =
		"http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";
	public final static String bogonsEmptyFeature =
		"http://www.ccil.org/~cowan/tagsoup/features/bogons-empty";
	public final static String lexicalHandlerProperty =
		"http://xml.org/sax/properties/lexical-handler";
	public final static String scannerProperty =
		"http://www.ccil.org/~cowan/tagsoup/properties/scanner";
	public final static String schemaProperty =
		"http://www.ccil.org/~cowan/tagsoup/properties/schema";
	public final static String autoDetectorProperty =
		"http://www.ccil.org/~cowan/tagsoup/properties/auto-detector";

	private HashMap theFeatures = new HashMap();
	{
		theFeatures.put(externalGeneralEntitiesFeature, Boolean.FALSE);
		theFeatures.put(externalParameterEntitiesFeature, Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/is-standalone",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/lexical-handler/parameter-entities",
			Boolean.FALSE);
		theFeatures.put(namespacesFeature, Boolean.TRUE);
		theFeatures.put(namespacePrefixesFeature, Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/resolve-dtd-uris",
			Boolean.TRUE);
		theFeatures.put("http://xml.org/sax/features/string-interning",
			Boolean.TRUE);
		theFeatures.put("http://xml.org/sax/features/use-attributes2",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/use-locator2",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/use-entity-resolver2",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/validation",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/xmlns-uris",
			Boolean.FALSE);
		theFeatures.put(ignoreBogonsFeature, Boolean.FALSE);
		theFeatures.put(bogonsEmptyFeature, Boolean.TRUE);
		}


	public boolean getFeature (String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
		Boolean b = (Boolean)theFeatures.get(name);
		if (b == null) {
			throw new SAXNotRecognizedException("Unknown feature " + name);
			}
		return b.booleanValue();
		}

	public void setFeature (String name, boolean value)
	throws SAXNotRecognizedException, SAXNotSupportedException {
		if (value)
			theFeatures.put(name, Boolean.TRUE);
		else
			theFeatures.put(name, Boolean.FALSE);
		}

	public Object getProperty (String name)
	throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals(lexicalHandlerProperty)) {
			return theLexicalHandler == this ? null : theLexicalHandler;
			}
		else if (name.equals(scannerProperty)) {
			return theScanner;
			}
		else if (name.equals(schemaProperty)) {
			return theSchema;
			}
		else if (name.equals(autoDetectorProperty)) {
			return theAutoDetector;
			}
		else {
			throw new SAXNotRecognizedException("Unknown property " + name);
			}
		}

	public void setProperty (String name, Object value)
	throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals(lexicalHandlerProperty)) {
			if (value instanceof LexicalHandler) {
				theLexicalHandler = (LexicalHandler)value;
				}
			else {
				throw new SAXNotSupportedException("Your lexical handler is not a LexicalHandler");
				}
			}
		else if (name.equals(scannerProperty)) {
			if (value instanceof Scanner) {
				theScanner = (Scanner)value;
				}
			else {
				throw new SAXNotSupportedException("Your scanner is not a Scanner");
				}
			}
		else if (name.equals(schemaProperty)) {
			if (value instanceof Schema) {
				theSchema = (Schema)value;
				}
			else {
				 throw new SAXNotSupportedException("Your schema is not a Schema");
				}
			}
		else if (name.equals(autoDetectorProperty)) {
			if (value instanceof AutoDetector) {
				theAutoDetector = (AutoDetector)value;
				}
			else {
				throw new SAXNotSupportedException("Your auto-detector is not an AutoDetector");
				}
			}
		else {
			throw new SAXNotRecognizedException("Unknown property " + name);
			}
		}

	public void setEntityResolver (EntityResolver resolver) {
		theEntityResolver = resolver;
		}

	public EntityResolver getEntityResolver () {
		return (theEntityResolver == this) ? null : theEntityResolver;
		}

	public void setDTDHandler (DTDHandler handler) {
		theDTDHandler = handler;
		}

	public DTDHandler getDTDHandler () {
		return (theDTDHandler == this) ? null : theDTDHandler;
		}

	public void setContentHandler (ContentHandler handler) {
		theContentHandler = handler;
		}

	public ContentHandler getContentHandler () {
		return (theContentHandler == this) ? null : theContentHandler;
		}

	public void setErrorHandler (ErrorHandler handler) {
		theErrorHandler = handler;
		}

	public ErrorHandler getErrorHandler () {
		return (theErrorHandler == this) ? null : theErrorHandler;
		}

	public void parse (InputSource input) throws IOException, SAXException {
		Reader r = input.getCharacterStream();
		setup();
		if (r == null) {
			r = getReader(input.getByteStream(), input.getEncoding(),
				input.getPublicId(), input.getSystemId());
			}
		theContentHandler.startDocument();
		if (!(theSchema.getURI().equals("")))
			theContentHandler.startPrefixMapping(theSchema.getPrefix(),
				theSchema.getURI());
		theScanner.scan(r, this);
		}

	public void parse (String systemId) throws IOException, SAXException {
		setup();
		Reader r = getReader(null, null, null, systemId);
		theContentHandler.startDocument();
		theScanner.scan(r, this);
		}

	// Sets up instance variables that haven't been set by setFeature
	private void setup() {
		if (theSchema == null) theSchema = HTMLSchema.sharedSchema();
		if (theScanner == null) theScanner = new HTMLScanner();
		if (theAutoDetector == null) {
			theAutoDetector = new AutoDetector() {
				public Reader autoDetectingReader(InputStream i) {
					return new InputStreamReader(i);
					}
				};
			}
		theStack = new Element(theSchema.getElementType("<root>"));
		thePCDATA = new Element(theSchema.getElementType("<pcdata>"));
		theNewElement = null;
		theAttributeName = null;
		thePITarget = null;
		theSaved = null;
		theEntity = 0;
		virginStack = true;
		}

	// Return a Reader based on the contents of an InputSource
	private Reader getReader(InputStream i, String encoding, String publicId, String systemId) throws IOException, SAXException {
		if (i == null) i = getInputStream(publicId, systemId);
		if (encoding == null) {
			return theAutoDetector.autoDetectingReader(i);
			}
		else {
			try {
				Reader r = new InputStreamReader(i, encoding);
				return r;
				}
			catch (UnsupportedEncodingException e) {
				return new InputStreamReader(i);
				}
			}
		}

	// Get an InputStream based on a publicId and a systemId
	private InputStream getInputStream(String publicId, String systemId) throws IOException, SAXException {
		URL basis = new URL("file", "", System.getProperty("user.dir") + "/.");
		URL url = new URL(basis, systemId);
		URLConnection c = url.openConnection();
		return c.getInputStream();
		}
		// We don't process publicIds (who uses them anyhow?)

	// ScanHandler implementation

	private Element theNewElement = null;
	private String theAttributeName = null;
	private String thePITarget = null;
	private Element theStack = null;
	private Element theSaved = null;
	private Element thePCDATA = null;
	private char theEntity = 0;

	public void adup(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement == null || theAttributeName == null) return;
		theNewElement.setAttribute(theAttributeName, null, theAttributeName);
		theAttributeName = null;
		}

	public void aname(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement == null) return;
		theAttributeName = makeName(buff, offset, length);
//		System.err.println("%% Attribute name " + theAttributeName);
		}

	public void aval(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement == null || theAttributeName == null) return;
		String value = new String(buff, offset, length);
//		System.err.println("%% Attribute value [" + value + "]");
		theNewElement.setAttribute(theAttributeName, null, value);
		theAttributeName = null;
//		System.err.println("%% Aval done");
		}

	public void entity(char[] buff, int offset, int length) throws SAXException {
		if (length < 1) {
			theEntity = 0;
			return;
			}
//		System.err.println("%% Entity at " + offset + " " + length);
		String name = new String(buff, offset, length);
//		System.err.println("%% Got entity [" + name + "]");
		theEntity = theSchema.getEntity(name);
		}

	public void eof(char[] buff, int offset, int length) throws SAXException {
		if (virginStack) rectify(thePCDATA);
		while (theStack.next() != null) {
			pop();
			}
		if (!(theSchema.getURI().equals("")))
			theContentHandler.endPrefixMapping(theSchema.getPrefix());
		theContentHandler.endDocument();
		}

	private static char[] etagchars = {'<', '/', '>'};
	public void etag(char[] buff, int offset, int length) throws SAXException {
		String lastName = theStack.name();
		// If this is a CDATA element and the tag doesn't match,
		// restart CDATA mode and process the tag as characters.
		if (((theStack.flags() & Schema.F_CDATA) != 0) &&
				length == lastName.length()) {
			boolean falseClose = false;
			for (int i = 0; i < length; i++) {
				if (buff[offset + i] != lastName.charAt(i)) {
					falseClose = true;
					break;
					}
				}
			if (falseClose) {
				theContentHandler.characters(etagchars, 0, 2);
				theContentHandler.characters(buff, offset, length);
				theContentHandler.characters(etagchars, 2, 1);
				theScanner.startCDATA();
				return;
				}
			}

		theNewElement = null;
		String name = makeName(buff, offset, length);
		// Handle empty tags and SGML end-tag minimization
		if (name == null || name.equals("")) {
			name = lastName;
			}
//		System.err.println("%% Got end of " + name);

		Element sp;
		boolean inNoforce = false;
		for (sp = theStack; sp != null; sp = sp.next()) {
			if (sp.name().equals(name)) break;
			if ((sp.flags() & Schema.F_NOFORCE) != 0) inNoforce = true;
			}

		if (sp == null) return;		// Ignore unknown etags
		if (sp.next() == null || sp.next().next() == null) return;
		if (inNoforce) {		// inside an F_NOFORCE element?
			sp.preclose();		// preclose the matching element
			}
		else {			// restartably pop everything above us
			while (theStack != sp) {
				restartablyPop();
				}
			pop();
			}
		// pop any preclosed elements now at the top
		while (theStack.isPreclosed()) {
			pop();
			}
		restart();
		}

	// Push restartables on the stack if possible
	private void restart() throws SAXException {
		while (theSaved != null && theStack.canContain(theSaved)) {
			Element next = theSaved.next();
			push(theSaved);
			theSaved = next;
			}
		}

	// Pop the stack irrevocably
	private void pop() throws SAXException {
		if (theStack == null) return;		// empty stack
		String name = theStack.name();
//		System.err.println("%% Popping " + name);
		if ((theStack.flags() & Schema.F_CDATA) != 0) {
			theLexicalHandler.endCDATA();
			}
		String namespace = theStack.namespace();
		if (theFeatures.get(namespacesFeature) == Boolean.FALSE) namespace = "";
		theContentHandler.endElement(namespace, theStack.localName(), name);
		theStack = theStack.next();
		}

	// Pop the stack restartably
	private void restartablyPop() throws SAXException {
		Element popped = theStack;
		pop();
		if ((popped.flags() & Schema.F_RESTART) != 0) {
			popped.anonymize();
			popped.setNext(theSaved);
			theSaved = popped;
			}
		}

	// Push element onto stack
	private boolean virginStack = true;
	private void push(Element e) throws SAXException {
//		System.err.println("%% Pushing " + e.name());
		e.clean();
		String namespace = e.namespace();
		if (theFeatures.get(namespacesFeature) == Boolean.FALSE) namespace = "";
		theContentHandler.startElement(namespace,
				e.localName(), e.name(), e.atts());
		e.setNext(theStack);
		theStack = e;
		virginStack = false;
		if ((theStack.flags() & Schema.F_CDATA) != 0) {
			theScanner.startCDATA();
			theLexicalHandler.startCDATA();
			}
		}

	public void gi(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement != null) return;
		String name = makeName(buff, offset, length);
		if (name == null) return;
		ElementType type = theSchema.getElementType(name);
		if (type == null) {
			// Suppress unknown elements if ignore-bogons is on
			if (theFeatures.get(ignoreBogonsFeature) == Boolean.TRUE)  {
				return;
				}
			boolean empty = theFeatures.get(bogonsEmptyFeature) == Boolean.TRUE;
			theSchema.elementType(name, empty ? Schema.M_EMPTY : Schema.M_ANY, Schema.M_ANY, 0);
			type = theSchema.getElementType(name);
			}

		// The root element mustn't be empty
		if ((theStack.name()).equals("<root>") && type.parent() == null) return;

		theNewElement = new Element(type);
//		System.err.println("%% Got GI " + theNewElement.name());
		}

	public void pcdata(char[] buff, int offset, int length) throws SAXException {
		if (length == 0) return;
		boolean allWhite = true;
		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(buff[offset+i])) {
				allWhite = false;
				}
			}
		if (allWhite && !theStack.canContain(thePCDATA)) return;
		rectify(thePCDATA);
		theContentHandler.characters(buff, offset, length);
		}

	public void pitarget(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement != null) return;
		thePITarget = makeName(buff, offset, length);
		}

	public void pi(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement != null || thePITarget == null) return;
		if (thePITarget.toLowerCase().equals("xml")) return;
		theContentHandler.processingInstruction(thePITarget,
			new String(buff, offset, length));
		thePITarget = null;
		}

	public void stagc(char[] buff, int offset, int length) throws SAXException {
		if (theNewElement == null) return;
		rectify(theNewElement);
		}

	public void cmnt(char[] buff, int offset, int length) throws SAXException {
		theLexicalHandler.comment(buff, offset, length);
		}

	// Rectify the stack, pushing and popping as needed
	// so that the argument can be safely pushed
	private void rectify(Element e) throws SAXException {
		Element sp;
		while (true) {
			for (sp = theStack; sp != null; sp = sp.next()) {
				if (sp.canContain(e)) break;
				}
			if (sp != null) break;
			ElementType parentType = e.parent();
			if (parentType == null) break;
			Element parent = new Element(parentType);
//			System.err.println("%% Ascending from " + e.name() + " to " + parent.name());
			parent.setNext(e);
			e = parent;
			}
		if (sp == null) return;		// don't know what to do
		while (theStack != sp) {
			if (theStack == null || theStack.next() == null ||
				theStack.next().next() == null) break;
			restartablyPop();
			}
		while (e != null) {
			Element nexte = e.next();
			if (!e.name().equals("<pcdata>")) push(e);
			e = nexte;
			restart();
			}
		theNewElement = null;
		}

	public char getEntity() {
		return theEntity;
		}

	// Return the argument as a valid XML name, lowercased
	private String makeName(char[] buff, int offset, int length) {
		StringBuffer dst = new StringBuffer(length + 2);
		boolean seenColon = false;
		boolean start = true;
//		String src = new String(buff, offset, length); // DEBUG
		for (; length-- > 0; offset++) {
			char ch = Character.toLowerCase(buff[offset]);
			if (Character.isLetter(ch) || ch == '_') {
				start = false;
				dst.append(ch);
				}
			else if (Character.isDigit(ch) || ch == '-' || ch == '.') {
				if (start) dst.append('_');
				start = false;
				dst.append(ch);
				}
			else if (ch == ':' && !seenColon) {
				seenColon = true;
				if (start) dst.append('_');
				start = true;
				dst.append(ch);
				}
			}
		int dstLength = dst.length();
		if (dstLength == 0 || dst.charAt(dstLength - 1) == ':') dst.append('_');
//		System.err.println("Made name \"" + dst + "\" from \"" + src + "\"");
		return dst.toString().intern();
		}

	// Default LexicalHandler implementation

	public void comment(char[] ch, int start, int length) throws SAXException { }
	public void endCDATA() throws SAXException { }
	public void endDTD() throws SAXException { }
	public void endEntity(String name) throws SAXException { }
	public void startCDATA() throws SAXException { }
	public void startDTD(String name, String publicId, String systemId) throws SAXException { }
	public void startEntity(String name) throws SAXException { }

	}
