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
// The TagSoup parser

package org.ccil.cowan.tagsoup;
import java.util.HashMap;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.megginson.sax.XMLWriter;

public class Parser extends DefaultHandler implements ScanHandler, XMLReader {

	// XMLReader implementation

	private ContentHandler theContentHandler = this;
	private DTDHandler theDTDHandler = this;
	private ErrorHandler theErrorHandler = this;
	private EntityResolver theEntityResolver = this;
	private Schema theSchema;
	private Scanner theScanner;
	private AutoDetector theAutoDetector;
	private HashMap theFeatures = new HashMap();
	{
		theFeatures.put("http://xml.org/sax/features/external-general-entities",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/external-parameter-entities",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/is-standalone",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/lexical-handler/parameter-entities",
			Boolean.FALSE);
		theFeatures.put("http://xml.org/sax/features/namespaces",
			Boolean.TRUE);
		theFeatures.put("http://xml.org/sax/features/namespace-prefixes",
			Boolean.FALSE);
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
		if (name.equals("http://xml.org/sax/features/namespaces") ||
		    name.equals("http://xml.org/sax/features/namespace-prefixes")) {
			// These features can be changed but have no effect
			if (value)
				theFeatures.put(name, Boolean.TRUE);
			else
				theFeatures.put(name, Boolean.FALSE);
			}
		else {
			// All other features are immutable
			boolean v = getFeature(name);
			if (v != value) {
				throw new SAXNotSupportedException("Can't change features");
				}
			}
		}

	public Object getProperty (String name)
	throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals("http://www.ccil.org/~cowan/tagsoup/properties/scanner")) {
			return theScanner;
			}
		else if (name.equals("http://www.ccil.org/~cowan/tagsoup/properties/schema")) {
			return theSchema;
			}
		else if (name.equals("http://www.ccil.org/~cowan/tagsoup/properties/auto-detector")) {
			return theAutoDetector;
			}
		else {
			throw new SAXNotRecognizedException("Unknown property " + name);
			}
		}

	public void setProperty (String name, Object value)
	throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals("http://www.ccil.org/~cowan/tagsoup/properties/scanner")) {
			if (value instanceof Scanner) {
				theScanner = (Scanner)value;
				}
			else {
				throw new SAXNotSupportedException("Your scanner is not a Scanner");
				}
			}
		else if (name.equals("http://www.ccil.org/~cowan/tagsoup/properties/schema")) {
			if (value instanceof Schema) {
				theSchema = (Schema)value;
				}
			else {
				 throw new SAXNotSupportedException("Your schema is not a Schema");
				}
			}
		else if (name.equals("http://www.ccil.org/~cowan/tagsoup/properties/auto-detector")) {
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

	public void adup(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement == null || theAttributeName == null) return;
		theNewElement.setAttribute(theAttributeName, null, theAttributeName);
		theAttributeName = null;
		}

	public void aname(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement == null) return;
		theAttributeName = alphatize(new String(buff, offset, length));
//		System.err.println("%% Attribute name " + theAttributeName);
		}

	public void aval(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement == null || theAttributeName == null) return;
		String value = new String(buff, offset, length);
//		System.err.println("%% Attribute value [" + value + "]");
		theNewElement.setAttribute(theAttributeName, null, value);
		theAttributeName = null;
//		System.err.println("%% Aval done");
		}

	public void entity(char[] buff, int offset, int length) throws IOException, SAXException {
//		System.err.println("%% Entity at " + offset + " " + length);
		String name = new String(buff, offset, length);
//		System.err.println("%% Got entity [" + name + "]");
		theEntity = theSchema.getEntity(name);
		}

	public void eof(char[] buff, int offset, int length) throws IOException, SAXException {
		while (theStack.next() != null) {
			pop();
			}
		theContentHandler.endPrefixMapping(theSchema.getPrefix());
		theContentHandler.endDocument();
		}

	public void etag(char[] buff, int offset, int length) throws IOException, SAXException {
		theNewElement = null;
		String name = alphatize(new String(buff, offset, length));
//		System.err.println("%% Got end of " + name);
		Element sp;
		for (sp = theStack; sp != null; sp = sp.next()) {
			if (sp.name().equals(name)) break;
			}
		if (sp == null) return;		// unknown etag, do nothing
		if (sp.next() == null) return;	// can't happen: matched <root>
		if (sp.next().next() == null) return;	// ignore outermost etag

		while (theStack != sp) {
			restartablyPop();
			}
		pop();
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
		String name = theStack.name();
//		System.err.println("%% Popping " + name);
		theContentHandler.endElement(theSchema.getURI(), name, name);
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
	private void push(Element e) throws SAXException {
//		System.err.println("%% Pushing " + e.name());
		e.clean();
		theContentHandler.startElement(theSchema.getURI(), e.name(), e.name(), e.atts());
		e.setNext(theStack);
		theStack = e;
		if ((theStack.flags() & Schema.F_CDATA) != 0) theScanner.startCDATA();
		}

	public void gi(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement != null) return;
		String name = alphatize(new String(buff, offset, length));
		if (name == null) return;
		ElementType type = theSchema.getElementType(name);
		if (type == null) {
			name = name.intern();
			theSchema.elementType(name, Schema.M_EMPTY, Schema.M_ANY, 0);
			type = theSchema.getElementType(name);
			}
		theNewElement = new Element(type);
//		System.err.println("%% Got GI " + theNewElement.name());
		}

	public void pcdata(char[] buff, int offset, int length) throws IOException, SAXException {
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

	public void pitarget(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement != null) return;
		thePITarget = new String(buff, offset, length).intern();
		}

	public void pi(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement != null || thePITarget == null) return;
		theContentHandler.processingInstruction(thePITarget,
			new String(buff, offset, length));
		thePITarget = null;
		}

	public void stagc(char[] buff, int offset, int length) throws IOException, SAXException {
		if (theNewElement == null) return;
		rectify(theNewElement);
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

	// Return the argument with non-alphanumerics stripped
	// and letters lowercased
	private String alphatize(String src) {
		StringBuffer dst = null;
		int len = src.length();
		for (int i = 0; i < len; i++) {
			char ch = Character.toLowerCase(src.charAt(i));
			if (Character.isLetterOrDigit(ch) || ch == ':') {
				if (dst == null) dst = new StringBuffer();
				dst.append(ch);
				}
			}
		if (dst == null) return null;
//		System.err.println("Alphatized \"" + src + "\" to \"" + dst + "\"");
		return dst.toString();
		}

	// Main method: tidies specified files or stdin
	// -Dfiles=true writes output to separate files with .xhtml extension
	// -Dpyx=true, -Dhtml=true uses Pyx or HTML output

	public static void main(String[] argv) throws IOException, SAXException {
		if (argv.length == 0) {
			tidy("/dev/stdin", System.out);
			}
		else if (Boolean.getBoolean("files")) {
			for (int i = 0; i < argv.length; i++) {
				String src = argv[i];
				String dst;
				int j = src.lastIndexOf('.');
				if (j == -1)
					dst = src + ".xhtml";
				else
					dst = src.substring(0, j) + ".xhtml";
				System.err.println("src: " + src + " dst: " + dst);
				OutputStream os = new FileOutputStream(dst);
				tidy(src, os);
				}
			}
		else {
			for (int i = 0; i < argv.length; i++) {
				System.err.println("src: " + argv[i]);
				tidy(argv[i], System.out);
				}
			}
		}

	private static void tidy(String src, OutputStream os)
			throws IOException, SAXException {
		XMLReader r = new Parser();
		Writer w = new OutputStreamWriter(os, "UTF-8");
		r.setContentHandler(chooseContentHandler(w));
		r.parse(src);
		}

	private static ContentHandler chooseContentHandler(Writer w) {
		ContentHandler h;
		if (Boolean.getBoolean("pyx"))
			h = new PYXWriter(w);
		else if (Boolean.getBoolean("html"))
			h = new HTMLWriter(w);
		else
			h = new XMLWriter(w);
		return h;
		}

	}
