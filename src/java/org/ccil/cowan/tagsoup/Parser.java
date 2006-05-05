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
	// Feature flags
	private boolean namespaces = true;
	private boolean namespacePrefixes = true;
	private boolean ignoreBogons = false;
	private boolean bogonsEmpty = true;
	private boolean defaultAttributes = true;

	/**
	A value of "true" indicates namespace URIs and unprefixed local
	names for element and attribute names will be available.
	**/
	public final static String namespacesFeature =
		"http://xml.org/sax/features/namespaces";

	/**
	A value of "true" indicates that XML qualified names (with prefixes)
	and attributes (including xmlns* attributes) will be available.
	**/
	public final static String namespacePrefixesFeature =
		"http://xml.org/sax/features/namespace-prefixes";

	/**
	Reports whether this parser processes external general entities
	(it doesn't).
	**/
	public final static String externalGeneralEntitiesFeature =
		"http://xml.org/sax/features/external-general-entities";

	/**
	Reports whether this parser processes external parameter entities
	(it doesn't).
	**/
	public final static String externalParameterEntitiesFeature =
		"http://xml.org/sax/features/external-parameter-entities";

	/**
	May be examined only during a parse, after the startDocument()
	callback has been completed; read-only. The value is true if
	the document specified standalone="yes" in its XML declaration,
	and otherwise is false.  (It's always false.)
	**/
	public final static String isStandaloneFeature =
		"http://xml.org/sax/features/is-standalone";

	/**
	A value of "true" indicates that the LexicalHandler will report
	the beginning and end of parameter entities (it won't).
	**/
	public final static String lexicalHandlerParameterEntitiesFeature =
		"http://xml.org/sax/features/lexical-handler/parameter-entities";

	/**
	A value of "true" indicates that system IDs in declarations will
	be absolutized (relative to their base URIs) before reporting.
	(This returns true but doesn't actually do anything.)
	**/
	public final static String resolveDTDURIsFeature =
		"http://xml.org/sax/features/resolve-dtd-uris";

	/**
	Has a value of "true" if all XML names (for elements,
	prefixes, attributes, entities, notations, and local
	names), as well as Namespace URIs, will have been interned
	using java.lang.String.intern. This supports fast testing of
	equality/inequality against string constants, rather than forcing
	slower calls to String.equals().  (We always intern.)
	**/
	public final static String stringInterningFeature =
		"http://xml.org/sax/features/string-interning";

	/**
	Returns "true" if the Attributes objects passed by this
	parser in ContentHandler.startElement() implement the
	org.xml.sax.ext.Attributes2 interface.	(They don't.)
	**/

	public final static String useAttributes2Feature =
		"http://xml.org/sax/features/use-attributes2";

	/**
	Returns "true" if the Locator objects passed by this parser
	in ContentHandler.setDocumentLocator() implement the
	org.xml.sax.ext.Locator2 interface.  (They don't.)
	**/
	public final static String useLocator2Feature =
		"http://xml.org/sax/features/use-locator2";

	/**
	Returns "true" if, when setEntityResolver is given an object
	implementing the org.xml.sax.ext.EntityResolver2 interface,
	those new methods will be used.  (They won't be.)
	**/
	public final static String useEntityResolver2Feature =
		"http://xml.org/sax/features/use-entity-resolver2";

	/**
	Controls whether the parser is reporting all validity errors
	(We don't report any validity errors.)
	**/
	public final static String validationFeature =
		"http://xml.org/sax/features/validation";

	/**
	Controls whether the parser reports Unicode normalization
	errors as described in section 2.13 and Appendix B of the XML
	1.1 Recommendation.  (We don't normalize.)
	**/
	public final static String unicodeNormalizationCheckingFeature =
"http://xml.org/sax/features/unicode-normalization-checking";

	/**
	Controls whether, when the namespace-prefixes feature is set,
	the parser treats namespace declaration attributes as being in
	the http://www.w3.org/2000/xmlns/ namespace.  (It doesn't.)
	**/
	public final static String xmlnsURIsFeature =
		"http://xml.org/sax/features/xmlns-uris";

	/**
	Returns "true" if the parser supports both XML 1.1 and XML 1.0.
	(Always false.)
	**/
	public final static String XML11Feature =
		"http://xml.org/sax/features/xml-1.1";

	/**
	A value of "true" indicates that the parser will ignore
	unknown elements.
	**/
	public final static String ignoreBogonsFeature =
		"http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";

	/**
	A value of "true" indicates that the parser will give unknown
	elements a content model of EMPTY; a value of "false", a
	content model of ANY.
	**/
	public final static String bogonsEmptyFeature =
		"http://www.ccil.org/~cowan/tagsoup/features/bogons-empty";

	/**
	A value of "true" indicates that the parser will return default
	attribute values for missing attributes that have default values.
	**/
	public final static String defaultAttributesFeature =
		"http://www.ccil.org/~cowan/tagsoup/features/default-attributes";

	/**
	Used to see some syntax events that are essential in some
	applications: comments, CDATA delimiters, selected general
	entity inclusions, and the start and end of the DTD (and
	declaration of document element name). The Object must implement
	org.xml.sax.ext.LexicalHandler.
	**/
	public final static String lexicalHandlerProperty =
		"http://xml.org/sax/properties/lexical-handler";

	/**
	Specifies the Scanner object this Parser uses.
	**/
	public final static String scannerProperty =
		"http://www.ccil.org/~cowan/tagsoup/properties/scanner";

	/**
	Specifies the Schema object this Parser uses.
	**/
	public final static String schemaProperty =
		"http://www.ccil.org/~cowan/tagsoup/properties/schema";

	/**
	Specifies the AutoDetector (for encoding detection) this Parser uses.
	**/
	public final static String autoDetectorProperty =
		"http://www.ccil.org/~cowan/tagsoup/properties/auto-detector";

	private HashMap theFeatures = new HashMap();
	{
		theFeatures.put(namespacesFeature, Boolean.TRUE);
		theFeatures.put(namespacePrefixesFeature, Boolean.TRUE);
		theFeatures.put(externalGeneralEntitiesFeature, Boolean.FALSE);
		theFeatures.put(externalParameterEntitiesFeature, Boolean.FALSE);
		theFeatures.put(isStandaloneFeature, Boolean.FALSE);
		theFeatures.put(lexicalHandlerParameterEntitiesFeature,
			Boolean.FALSE);
		theFeatures.put(resolveDTDURIsFeature, Boolean.TRUE);
		theFeatures.put(stringInterningFeature, Boolean.TRUE);
		theFeatures.put(useAttributes2Feature, Boolean.FALSE);
		theFeatures.put(useLocator2Feature, Boolean.FALSE);
		theFeatures.put(useEntityResolver2Feature, Boolean.FALSE);
		theFeatures.put(validationFeature, Boolean.FALSE);
		theFeatures.put(xmlnsURIsFeature, Boolean.FALSE);
		theFeatures.put(xmlnsURIsFeature, Boolean.FALSE);
		theFeatures.put(XML11Feature, Boolean.FALSE);
		theFeatures.put(ignoreBogonsFeature, Boolean.FALSE);
		theFeatures.put(bogonsEmptyFeature, Boolean.TRUE);
		theFeatures.put(defaultAttributesFeature, Boolean.TRUE);
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
		if (value) theFeatures.put(name, Boolean.TRUE);
		else theFeatures.put(name, Boolean.FALSE);

		if (name.equals(namespacesFeature)) namespaces = value;
		else if (name.equals(namespacePrefixesFeature)) namespacePrefixes = value;
		else if (name.equals(ignoreBogonsFeature)) ignoreBogons = value;
		else if (name.equals(bogonsEmptyFeature)) bogonsEmpty = value;
		else if (name.equals(defaultAttributesFeature)) defaultAttributes = value;
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
		setup();
		Reader r = getReader(input);
		theContentHandler.startDocument();
		theScanner.resetDocumentLocator(input.getPublicId(), input.getSystemId());
		if (theScanner instanceof Locator) {
			theContentHandler.setDocumentLocator((Locator)theScanner);
			}
		if (!(theSchema.getURI().equals("")))
			theContentHandler.startPrefixMapping(theSchema.getPrefix(),
				theSchema.getURI());
		theScanner.scan(r, this);
		}

	public void parse (String systemId) throws IOException, SAXException {
		parse(new InputSource(systemId));
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
	// Buffer both the InputStream and the Reader
	private Reader getReader(InputSource s) throws SAXException, IOException {
		Reader r = s.getCharacterStream();
		InputStream i = s.getByteStream();
		String encoding = s.getEncoding();
		String publicId = s.getPublicId();
		String systemId = s.getSystemId();
		if (r == null) {
			if (i == null) i = getInputStream(publicId, systemId);
//			i = new BufferedInputStream(i);
			if (encoding == null) {
				r = theAutoDetector.autoDetectingReader(i);
				}
			else {
				try {
					r = new InputStreamReader(i, encoding);
					}
				catch (UnsupportedEncodingException e) {
					r = new InputStreamReader(i);
					}
				}
			}
//		r = new BufferedReader(r);
		return r;
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

	public void etag(char[] buff, int offset, int length) throws SAXException {
		if (etag_cdata(buff, offset, length)) return;
		etag_basic(buff, offset, length);
		}

	private static char[] etagchars = {'<', '/', '>'};
	public boolean etag_cdata(char[] buff, int offset, int length) throws SAXException {
		String currentName = theStack.name();
		// If this is a CDATA element and the tag doesn't match,
		// or isn't properly formed (junk after the name),
		// restart CDATA mode and process the tag as characters.
		if ((theStack.flags() & Schema.F_CDATA) != 0) {
			boolean realTag = (length == currentName.length());
			if (realTag) {
				for (int i = 0; i < length; i++) {
					if (Character.toLowerCase(buff[offset + i]) != Character.toLowerCase(currentName.charAt(i))) {
						realTag = false;
						break;
						}
					}
				}
			if (!realTag) {
				theContentHandler.characters(etagchars, 0, 2);
				theContentHandler.characters(buff, offset, length);
				theContentHandler.characters(etagchars, 2, 1);
				theScanner.startCDATA();
				return true;
				}
			}
		return false;
		}

	public void etag_basic(char[] buff, int offset, int length) throws SAXException {
		theNewElement = null;
		String name;
		if (length != 0) name = makeName(buff, offset, length);
		else name = theStack.name();
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
		restart(null);
		}

	// Push restartables on the stack if possible
	// e is the next element to be started, if we know what it is
	private void restart(Element e) throws SAXException {
		while (theSaved != null && theStack.canContain(theSaved) &&
				(e == null || theSaved.canContain(e))) {
			Element next = theSaved.next();
			push(theSaved);
			theSaved = next;
			}
		}

	// Pop the stack irrevocably
	private void pop() throws SAXException {
		if (theStack == null) return;		// empty stack
		String name = theStack.name();
		String localName = theStack.localName();
//		System.err.println("%% Popping " + name);
		if ((theStack.flags() & Schema.F_CDATA) != 0) {
			theLexicalHandler.endCDATA();
			}
		String namespace = theStack.namespace();
		if (!namespaces) namespace = "";
		if (!namespacePrefixes) name = "";
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
		String name = e.name();
		String localName = e.localName();
//		System.err.println("%% Pushing " + name);
		e.clean(defaultAttributes);
		String namespace = e.namespace();
		if (!namespaces) namespace = "";
		if (!namespacePrefixes) name = "";
		theContentHandler.startElement(namespace, localName, name, e.atts());
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
			if (ignoreBogons) return;
			theSchema.elementType(name, bogonsEmpty ? Schema.M_EMPTY : Schema.M_ANY, Schema.M_ANY, 0);
			type = theSchema.getElementType(name);
			}

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
//		if (length > 0 && buff[length - 1] == '?') System.out.println("%% Removing ? from PI");
		if (length > 0 && buff[length - 1] == '?') length--;	// remove trailing ?
		theContentHandler.processingInstruction(thePITarget,
			new String(buff, offset, length));
		thePITarget = null;
		}

	public void stagc(char[] buff, int offset, int length) throws SAXException {
//		System.err.println("%% Start-tag");
		if (theNewElement == null) return;
		rectify(theNewElement);
		if (theStack.model() == Schema.M_EMPTY) {
			// Force an immediate end tag
			etag_basic(buff, offset, length);
			}
		}

	public void stage(char[] buff, int offset, int length) throws SAXException {
//		System.err.println("%% Empty-tag");
		if (theNewElement == null) return;
		rectify(theNewElement);
		// Force an immediate end tag
		etag_basic(buff, offset, length);
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
			restart(e);
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
