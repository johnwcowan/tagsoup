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
// Scanner

package org.ccil.cowan.tagsoup;
import java.io.IOException;
import java.io.Reader;
import org.xml.sax.SAXException;

public interface Scanner {

	public void scan(Reader r, ScanHandler h) throws IOException, SAXException;
	public void startCDATA();

	}
