// This file is part of TagSoup.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.  You may also distribute
// and/or modify it under version 1.2 of the Academic Free License.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// 
// 
// PYX Writer
// FIXME: does not do escapes in attribute values
// FIXME: outputs entities as bare '&' character

package org.ccil.cowan.tagsoup;
import java.io.*;
import org.xml.sax.*;

public class PYXWriter
	implements ScanHandler, ContentHandler {

	private PrintWriter theWriter;		// where we write to
	private static char[] dummy = new char[1];
	private String attrName;		// saved attribute name

	// ScanHandler implementation

	public void adup(char[] buff, int offset, int length) throws IOException {
		theWriter.println(attrName);
		attrName = null;
		}

	public void aname(char[] buff, int offset, int length) throws IOException {
		theWriter.print('A');
		theWriter.write(buff, offset, length);
		theWriter.print(' ');
		attrName = new String(buff, offset, length);
		}

	public void aval(char[] buff, int offset, int length) throws IOException {
		theWriter.write(buff, offset, length);
		theWriter.println();
		attrName = null;
		}

	public void entity(char[] buff, int offset, int length) throws IOException { }

	public char getEntity() { return 0; }

	public void eof(char[] buff, int offset, int length) throws IOException {
		theWriter.close();
		}

	public void etag(char[] buff, int offset, int length) throws IOException {
		theWriter.print(')');
		theWriter.write(buff, offset, length);
		theWriter.println();
		}

	public void gi(char[] buff, int offset, int length) throws IOException {
		theWriter.print('(');
		theWriter.write(buff, offset, length);
		theWriter.println();
		}

	public void pcdata(char[] buff, int offset, int length) throws IOException {
		if (length == 0) return;	// nothing to do
		boolean inProgress = false;
		length += offset;
		for (int i = offset; i < length; i++) {
			if (buff[i] == '\n') {
				if (inProgress) {
					theWriter.println();
					}
				theWriter.println("-\\n");
				inProgress = false;
				}
			else {
				if (!inProgress) {
					theWriter.print('-');
					}
				switch(buff[i]) {
				case '\t':
					theWriter.print("\\t");
					break;
				case '\\':
					theWriter.print("\\\\");
					break;
				default:
					theWriter.print(buff[i]);
					}
				inProgress = true;
				}
			}
		if (inProgress) {
			theWriter.println();
			}
		}

	public void pitarget(char[] buff, int offset, int length) throws IOException {
		theWriter.print('?');
		theWriter.write(buff, offset, length);
		theWriter.write(' ');
		}

	public void pi(char[] buff, int offset, int length) throws IOException {
		theWriter.write(buff, offset, length);
		theWriter.println();
		}

	public void stagc(char[] buff, int offset, int length) throws IOException {
		theWriter.println("!");			// FIXME
		}

	// SAX ContentHandler implementation

	public void characters(char[] buff, int offset, int length) {
		try {
			pcdata(buff, offset, length);
			}
		catch (IOException e) { }	// can't actually throw this
		}

	public void endDocument() {
		theWriter.close();
		}

	public void endElement(String uri, String localname, String qname) {
		if (qname.length() == 0) qname = localname;
		theWriter.print(')');
		theWriter.println(qname);
		}

	public void endPrefixMapping(String prefix) { }

	public void ignorableWhitespace(char[] buff, int offset, int length) {
		characters(buff, offset, length);
		}

	public void processingInstruction(String target, String data) {
		theWriter.print('?');
		theWriter.print(target);
		theWriter.print(' ');
		theWriter.println(data);
		}

	public void setDocumentLocator(Locator locator) { }

	public void skippedEntity(String name) { }

	public void startDocument() { }

	public void startElement(String uri, String localname, String qname,
			Attributes atts) {
		if (qname.length() == 0) qname=localname;
		theWriter.print('(');
		theWriter.println(qname);
		int length = atts.getLength();
		for (int i = 0; i < length; i++) {
			qname = atts.getQName(i);
			if (qname.length() == 0) qname = atts.getLocalName(i);
			theWriter.print('A');
			theWriter.print(qname);
			theWriter.print(' ');
			theWriter.println(atts.getValue(i));
			}
		}

	public void startPrefixMapping(String prefix, String uri) { }

	// Constructor

	public PYXWriter(Writer w) {
		if (w instanceof PrintWriter) {
			theWriter = (PrintWriter)w;
			}
		else {
			theWriter = new PrintWriter(w);
			}
		}
	}
