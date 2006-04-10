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
package org.ccil.cowan.tagsoup;
import java.io.*;
import org.xml.sax.SAXException;

/**
This class implements a table-driven scanner for HTML, allowing for lots of
defects.  It implements the Scanner interface, which accepts a Reader
object to fetch characters from and a ScanHandler object to report lexical
events to.
*/

public class HTMLScanner implements Scanner {

	// Start of state table
	@@STATE_TABLE@@
	// End of state table

	int theState;					// Current state
	int theNextState;				// Next state
	char[] theOutputBuffer = new char[2000];	// Output buffer
	int theSize;					// Current buffer size
	int[] theWinMap = {				// Windows chars map
		0x20AC, 0xFFFD, 0x201A, 0x0192, 0x201E, 0x2026, 0x2020, 0x2021,
		0x02C6, 0x2030, 0x0160, 0x2039, 0x0152, 0xFFFD, 0x017D, 0xFFFD,
		0xFFFD, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022, 0x2013, 0x2014,
		0x02DC, 0x2122, 0x0161, 0x203A, 0x0153, 0xFFFD, 0x017E, 0x0178};


	// Scanner implementation

	/**
	Scan HTML source, reporting lexical events.
	@param r0 Reader that provides characters
	@param h ScanHandler that accepts lexical events.
	*/


	public void scan(Reader r0, ScanHandler h) throws IOException, SAXException {
		theState = S_PCDATA;
		int savedState = 0;
		PushbackReader r;
		if (r0 instanceof PushbackReader) {
			r = (PushbackReader)r0;
			}
		else if (r0 instanceof BufferedReader) {
			r = new PushbackReader(r0);
			}
		else {
			r = new PushbackReader(new BufferedReader(r0));
			}

		while (theState != S_DONE) {
			int ch = r.read();
			if (ch >= 0x80 && ch <= 0x9F) ch = theWinMap[ch-0x80];
			if (ch < 0x20 && ch != '\n' && ch != '\r' && ch != '\t' && ch != -1) continue;
			// Search state table
			int action = 0;
			for (int i = 0; i < statetable.length; i += 4) {
				if (theState != statetable[i]) {
					if (action != 0) break;
					continue;
					}
				if (statetable[i+1] == 0) {
					action = statetable[i+2];
					theNextState = statetable[i+3];
					}
				else if (statetable[i+1] == ch ||
				    (statetable[i+1] == ' ' && ch <= ' ')) {
					action = statetable[i+2];
					theNextState = statetable[i+3];
					break;
					}
				}
			switch (action) {
			case 0:
				throw new Error(
"HTMLScanner can't cope with " + Integer.toString(ch) + " in state " +
Integer.toString(theState));
        		case A_ADUP:
				h.adup(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_ADUP_SAVE:
				h.adup(theOutputBuffer, 0, theSize);
				theSize = 0;
				save(ch, h);
				break;
        		case A_ADUP_STAGC:
				h.adup(theOutputBuffer, 0, theSize);
				theSize = 0;
				h.stagc(theOutputBuffer, 0, theSize);
				break;
        		case A_ANAME:
				h.aname(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_ANAME_ADUP_STAGC:
				h.aname(theOutputBuffer, 0, theSize);
				theSize = 0;
				h.adup(theOutputBuffer, 0, theSize);
				break;
        		case A_AVAL:
				h.aval(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_AVAL_STAGC:
				h.aval(theOutputBuffer, 0, theSize);
				theSize = 0;
				h.stagc(theOutputBuffer, 0, theSize);
				break;
			case A_ENTSAVE:
				char ch1 = (char)ch;
				if (Character.isLetterOrDigit(ch1) ||
				    ch1 == '&' || ch1 == '#') {
					save(ch, h);
					break;
					}
				else {
					if (ch != ';') r.unread(ch);
					// fall through into A_ENTITY_POP
					}
        		case A_ENTITY_POP:
//				System.err.println("%%" + new String(theOutputBuffer, 0, theSize));
				h.entity(theOutputBuffer, 0, theSize);
				char c = h.getEntity();
				if (c != 0) {
					theSize = 0;
					if (ch >= 0x80 && ch <= 0x9F) {
						ch = theWinMap[ch-0x80];
						}
					save(c, h);
					}
				theNextState = savedState;
				break;
        		case A_ETAG:
				h.etag(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_GI:
				h.gi(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
			case A_GI_STAGC:
				h.gi(theOutputBuffer, 0, theSize);
				theSize = 0;
				h.stagc(theOutputBuffer, 0, theSize);
				break;
        		case A_LF:
				save('\n', h);
				break;
        		case A_LT:
				save('<', h);
				break;
			case A_LT_PCDATA:
				save('<', h);
				h.pcdata(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_PCDATA:
				h.pcdata(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_PI:
				h.pi(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_PITARGET:
				h.pitarget(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
        		case A_PITARGET_PI:
				h.pitarget(theOutputBuffer, 0, theSize);
				theSize = 0;
				h.pi(theOutputBuffer, 0, theSize);
				break;
			case A_PCDATA_SAVE_PUSH:
				h.pcdata(theOutputBuffer, 0, theSize);
				// fall through into A_SAVE_PUSH
        		case A_SAVE_PUSH:
				savedState = theState;
				save(ch, h);
				break;
        		case A_SAVE:
				save(ch, h);
				break;
        		case A_SKIP:
				break;
        		case A_SP:
				save(' ', h);
				break;
        		case A_STAGC:
				h.stagc(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
			case A_UNGET:
				r.unread(ch);
				break;
        		case A_UNSAVE_PCDATA:
				if (theSize > 0) theSize--;
				h.pcdata(theOutputBuffer, 0, theSize);
				theSize = 0;
				break;
			default:
				throw new Error("Can't process state " + action);
				}
			theState = theNextState;
			}
		h.eof(theOutputBuffer, 0, 0);
		}


	/**
	A callback for the ScanHandler that allows it to force
	the lexer state to CDATA content (no markup is recognized except
	the end of element.
	*/

	public void startCDATA() { theNextState = S_CDATA; }

	private void save(int ch, ScanHandler h) throws IOException, SAXException {
		if (theSize >= theOutputBuffer.length - 20) {
			if (theState == S_PCDATA || theState == S_CDATA) {
				h.pcdata(theOutputBuffer, 0, theSize);
				theSize = 0;
				}
			else {
				return;
				}
			}
		theOutputBuffer[theSize++] = (char)ch;
		}

	/**
	Test procedure.  Reads HTML from the standard input and writes
	PYX to the standard output.
	*/

	public static void main(String[] argv) throws IOException, SAXException {
		Scanner s = new HTMLScanner();
		Reader r = new InputStreamReader(System.in, "UTF-8");
		Writer w = new OutputStreamWriter(System.out, "UTF-8");
		PYXWriter pw = new PYXWriter(w);
		s.scan(r, pw);
		w.close();
		}

	}
