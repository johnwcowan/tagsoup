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
// Scanner handler

package org.ccil.cowan.tagsoup;
import org.xml.sax.SAXException;

public interface ScanHandler {
	public void adup(char[] buff, int offset, int length) throws SAXException;
	public void aname(char[] buff, int offset, int length) throws SAXException;
	public void aval(char[] buff, int offset, int length) throws SAXException;
	public void entity(char[] buff, int offset, int length) throws SAXException;
	public void eof(char[] buff, int offset, int length) throws SAXException;
	public void etag(char[] buff, int offset, int length) throws SAXException;
	public void gi(char[] buff, int offset, int length) throws SAXException;
	public void pcdata(char[] buff, int offset, int length) throws SAXException;
	public void pi(char[] buff, int offset, int length) throws SAXException;
	public void pitarget(char[] buff, int offset, int length) throws SAXException;
	public void stagc(char[] buff, int offset, int length) throws SAXException;
	public void cmnt(char[] buff, int offset, int length) throws SAXException;
	public char getEntity();
	}
