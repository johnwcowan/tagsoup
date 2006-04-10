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
// XML Writer

package org.ccil.cowan.tagsoup;
import java.io.*;
import java.util.Stack;
import org.xml.sax.*;

public class XMLWriter
	implements ContentHandler {

	private PrintWriter theWriter;		// where we write to
	private boolean htmlMode = false;
	private boolean newlineMode = false;
	private boolean cdataMode = false;
	Stack theStack = new Stack();

	// SAX ContentHandler implementation

	public void characters(char[] buff, int offset, int length) {
		for (int i = 0; i < length; i++) {
			if (!cdataMode) {
				switch (buff[offset+i]) {
				case '<':
					theWriter.print("&lt;");
					break;
				case '>':
					theWriter.print("&gt;");
					break;
				case '&':
					theWriter.print("&amp;");
					break;
				case '\n':
					theWriter.println();
					break;
				default:
					theWriter.print(buff[offset+i]);
					}
				}
			}
		}

	public void endDocument() {
		theWriter.println();
		theWriter.close();
		}

	public void endElement(String uri, String localname, String qname) {
		if (qname.length() == 0) qname = localname;
		String expected = (String)theStack.pop();
		if (!localname.equals(expected)) {
			throw new Error("XMLWriter: expected " + expected +
				" got " + localname);
			}
		if (htmlMode) {
			if (qname.equals("area")) return;
			if (qname.equals("base")) return;
			if (qname.equals("basefont")) return;
			if (qname.equals("br")) return;
			if (qname.equals("col")) return;
			if (qname.equals("frame")) return;
			if (qname.equals("hr")) return;
			if (qname.equals("image")) return;
			if (qname.equals("input")) return;
			if (qname.equals("isindex")) return;
			if (qname.equals("link")) return;
			if (qname.equals("meta")) return;
			if (qname.equals("param")) return;
			}
		theWriter.print("</");
		theWriter.print(qname);
		theWriter.print('>');
		if (newlineMode) theWriter.println();
		cdataMode = false;
		}

	public void endPrefixMapping(String prefix) { }

	public void ignorableWhitespace(char[] buff, int offset, int length) {
		characters(buff, offset, length);
		}

	public void processingInstruction(String target, String data) {
		theWriter.print("<?");
		theWriter.print(target);
		theWriter.print(' ');
		theWriter.print(data);
		theWriter.print("?>");
		if (newlineMode) theWriter.println();
		}

	public void setDocumentLocator(Locator locator) { }

	public void skippedEntity(String name) { }

	public void startDocument() { }

	public void startElement(String uri, String localname, String qname,
			Attributes atts) {
		if (qname.length() == 0) qname=localname;
		theWriter.print('<');
		theWriter.print(qname);
		theStack.push(qname);
		if (newlineMode) theWriter.println();
		int length = atts.getLength();
		for (int i = 0; i < length; i++) {
			theWriter.print(' ');
			String attname = atts.getQName(i);
			if (attname.length() == 0) attname = atts.getLocalName(i);
			theWriter.print(attname);
			theWriter.print(" = \"");
			String value = atts.getValue(i);
			int valueLength = value.length();
			for (int j = 0; j < valueLength; j++) {
				char attchar = value.charAt(j);
				switch (attchar) {
				case '<':
					theWriter.print("&lt;");
					break;
				case '&':
					theWriter.print("&amp;");
					break;
				case '"':
					theWriter.print("&quot;");
					break;
				default:
					theWriter.print(attchar);
					}
				}
			theWriter.print('"');
			if (newlineMode) theWriter.println();
			}
		theWriter.print('>');
		if (newlineMode) theWriter.println();
		if (htmlMode && (qname.equals("script") || qname.equals("style"))) {
			cdataMode = true;
			}
		}

	public void startPrefixMapping(String prefix, String uri) { }

	// Constructor

	public XMLWriter(Writer w) {
		if (w instanceof PrintWriter) {
			theWriter = (PrintWriter)w;
			}
		else {
			theWriter = new PrintWriter(w);
			}
		}

	public void setHTMLMode(boolean b) {
		htmlMode = b;
		}
	
	public void setNewlineMode(boolean b) {
		newlineMode = b;
		}

	public boolean getHTMLMode() {
		return htmlMode;
		}

	public boolean getNewlineMode() {
		return newlineMode;
		}
	}
