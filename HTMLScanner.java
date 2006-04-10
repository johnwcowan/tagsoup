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
	private static final int S_ANAME = 1;
	private static final int S_APOS = 2;
	private static final int S_AVAL = 3;
	private static final int S_CCRLF = 4;
	private static final int S_CDATA = 5;
	private static final int S_CDATA2 = 6;
	private static final int S_COM = 7;
	private static final int S_COMD = 8;
	private static final int S_CRLF = 9;
	private static final int S_DECL = 10;
	private static final int S_DECL2 = 11;
	private static final int S_DONE = 12;
	private static final int S_ENT = 13;
	private static final int S_EQ = 14;
	private static final int S_ETAG = 15;
	private static final int S_GI = 16;
	private static final int S_PCDATA = 17;
	private static final int S_PI = 18;
	private static final int S_PITARGET = 19;
	private static final int S_QUOT = 20;
	private static final int S_STAGC = 21;
	private static final int S_TAG = 22;
	private static final int S_TAGWS = 23;
	private static final int A_ADUP = 1;
	private static final int A_ADUP_SAVE = 2;
	private static final int A_ADUP_STAGC = 3;
	private static final int A_ANAME = 4;
	private static final int A_ANAME_ADUP_STAGC = 5;
	private static final int A_AVAL = 6;
	private static final int A_AVAL_STAGC = 7;
	private static final int A_ENTITY_POP = 8;
	private static final int A_ENTSAVE = 9;
	private static final int A_ETAG = 10;
	private static final int A_GI = 11;
	private static final int A_GI_STAGC = 12;
	private static final int A_LF = 13;
	private static final int A_LT = 14;
	private static final int A_LT_PCDATA = 15;
	private static final int A_PCDATA = 16;
	private static final int A_PCDATA_SAVE_PUSH = 17;
	private static final int A_PI = 18;
	private static final int A_PITARGET = 19;
	private static final int A_PITARGET_PI = 20;
	private static final int A_SAVE = 21;
	private static final int A_SAVE_PUSH = 22;
	private static final int A_SKIP = 23;
	private static final int A_SP = 24;
	private static final int A_STAGC = 25;
	private static final int A_UNGET = 26;
	private static final int A_UNSAVE_PCDATA = 27;
	private static int[] statetable = {
		S_ANAME, 0, A_SAVE, S_ANAME,
		S_ANAME, '=', A_ANAME, S_AVAL,
		S_ANAME, '>', A_ANAME_ADUP_STAGC, S_PCDATA,
		S_ANAME, -1, A_ANAME_ADUP_STAGC, S_DONE,
		S_ANAME, ' ', A_ANAME, S_EQ,
		S_APOS, 0, A_SAVE, S_APOS,
		S_APOS, '&', A_SAVE_PUSH, S_ENT,
		S_APOS, '\'', A_AVAL, S_TAGWS,
		S_APOS, -1, A_AVAL_STAGC, S_DONE,
		S_APOS, ' ', A_SP, S_APOS,
		S_AVAL, 0, A_SAVE, S_STAGC,
		S_AVAL, '"', A_SKIP, S_QUOT,
		S_AVAL, '\'', A_SKIP, S_APOS,
		S_AVAL, '>', A_AVAL_STAGC, S_PCDATA,
		S_AVAL, -1, A_AVAL_STAGC, S_DONE,
		S_AVAL, ' ', A_SKIP, S_AVAL,
		S_CDATA, 0, A_SAVE, S_CDATA,
		S_CDATA, '<', A_SAVE, S_CDATA2,
		S_CDATA, '\r', A_LF, S_CCRLF,
		S_CDATA, -1, A_PCDATA, S_DONE,
		S_CDATA2, 0, A_SAVE, S_CDATA,
		S_CDATA2, '/', A_UNSAVE_PCDATA, S_ETAG,
		S_CDATA2, -1, A_UNSAVE_PCDATA, S_DONE,
		S_COM, 0, A_SKIP, S_COM,
		S_COM, '-', A_SKIP, S_COMD,
		S_COM, -1, A_SKIP, S_DONE,
		S_COMD, 0, A_SKIP, S_COM,
		S_COMD, '-', A_SKIP, S_COMD,
		S_COMD, '>', A_SKIP, S_PCDATA,
		S_COMD, -1, A_SKIP, S_DONE,
		S_CRLF, 0, A_UNGET, S_PCDATA,
		S_CRLF, '\n', A_SKIP, S_PCDATA,
		S_CRLF, -1, A_SKIP, S_DONE,
		S_CCRLF, 0, A_UNGET, S_CDATA,
		S_CCRLF, '\n', A_SKIP, S_CDATA,
		S_CCRLF, '\n', A_SKIP, S_DONE,
		S_DECL, 0, A_SKIP, S_DECL2,
		S_DECL, '-', A_SKIP, S_COM,
		S_DECL, '>', A_SKIP, S_PCDATA,
		S_DECL, -1, A_SKIP, S_DONE,
		S_DECL2, 0, A_SKIP, S_DECL2,
		S_DECL2, '>', A_SKIP, S_PCDATA,
		S_DECL2, -1, A_SKIP, S_DONE,
		S_ENT, 0, A_ENTSAVE, S_ENT,
		S_ENT, -1, A_ENTITY_POP, S_DONE,
		S_EQ, 0, A_ADUP_SAVE, S_ANAME,
		S_EQ, '=', A_SKIP, S_AVAL,
		S_EQ, '>', A_ADUP, S_PCDATA,
		S_EQ, -1, A_ADUP_STAGC, S_DONE,
		S_EQ, ' ', A_SKIP, S_EQ,
		S_ETAG, 0, A_SAVE, S_ETAG,
		S_ETAG, '>', A_ETAG, S_PCDATA,
		S_ETAG, -1, A_ETAG, S_DONE,
		S_ETAG, ' ', A_SKIP, S_ETAG,
		S_GI, 0, A_SAVE, S_GI,
		S_GI, '/', A_SKIP, S_GI,
		S_GI, '>', A_GI_STAGC, S_PCDATA,
		S_GI, -1, A_SKIP, S_DONE,
		S_GI, ' ', A_GI, S_TAGWS,
		S_PCDATA, 0, A_SAVE, S_PCDATA,
		S_PCDATA, '&', A_PCDATA_SAVE_PUSH, S_ENT,
		S_PCDATA, '<', A_PCDATA, S_TAG,
		S_PCDATA, '\r', A_LF, S_CRLF,
		S_PCDATA, -1, A_PCDATA, S_DONE,
		S_PCDATA, ' ', A_SP, S_PCDATA,
		S_PI, 0, A_SAVE, S_PI,
		S_PI, '>', A_PI, S_PCDATA,
		S_PI, -1, A_PI, S_DONE,
		S_PITARGET, 0, A_SAVE, S_PITARGET,
		S_PITARGET, '>', A_PITARGET_PI, S_PCDATA,
		S_PITARGET, -1, A_PITARGET_PI, S_DONE,
		S_PITARGET, ' ', A_PITARGET, S_PI,
		S_QUOT, 0, A_SAVE, S_QUOT,
		S_QUOT, '"', A_AVAL, S_TAGWS,
		S_QUOT, '&', A_SAVE_PUSH, S_ENT,
		S_QUOT, -1, A_AVAL_STAGC, S_DONE,
		S_QUOT, ' ', A_SP, S_QUOT,
		S_STAGC, 0, A_SAVE, S_STAGC,
		S_STAGC, '>', A_AVAL_STAGC, S_PCDATA,
		S_STAGC, -1, A_AVAL_STAGC, S_DONE,
		S_STAGC, ' ', A_AVAL, S_TAGWS,
		S_TAG, 0, A_SAVE, S_GI,
		S_TAG, '!', A_SKIP, S_DECL,
		S_TAG, '/', A_SKIP, S_ETAG,
		S_TAG, '?', A_SKIP, S_PITARGET,
		S_TAG, -1, A_LT_PCDATA, S_DONE,
		S_TAG, ' ', A_LT, S_PCDATA,
		S_TAGWS, 0, A_SAVE, S_ANAME,
		S_TAGWS, '/', A_SKIP, S_TAGWS,
		S_TAGWS, '>', A_STAGC, S_PCDATA,
		S_TAGWS, -1, A_STAGC, S_DONE,
		S_TAGWS, ' ', A_SKIP, S_TAGWS,
		};
// Actions: A_ADUP, A_ADUP_SAVE, A_ADUP_STAGC, A_ANAME, A_ANAME_ADUP_STAGC, A_AVAL, A_AVAL_STAGC, A_ENTITY_POP, A_ENTSAVE, A_ETAG, A_GI, A_GI_STAGC, A_LF, A_LT, A_LT_PCDATA, A_PCDATA, A_PCDATA_SAVE_PUSH, A_PI, A_PITARGET, A_PITARGET_PI, A_SAVE, A_SAVE_PUSH, A_SKIP, A_SP, A_STAGC, A_UNGET, A_UNSAVE_PCDATA

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
