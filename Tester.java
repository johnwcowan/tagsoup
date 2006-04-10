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
// Test rig for parsing lots of files

package org.ccil.cowan.tagsoup;
import java.util.HashMap;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class Tester {

	// Test harness

	public static void main(String[] argv) throws IOException, SAXException {
		StringBuffer flags = new StringBuffer();
		if (Boolean.getBoolean("html")) flags.append('h');
		if (Boolean.getBoolean("newline")) flags.append('n');
		for (int fileno = 0; fileno < argv.length; fileno++) {
			XMLReader r = new Parser();
			String source = argv[fileno];
			String dst = "x" + source.substring(2);
			OutputStream os = new FileOutputStream(dst);
			ContentHandler h = new XMLWriter(new OutputStreamWriter
				(os, "UTF-8"), flags.toString());
			r.setContentHandler(h);
			System.err.println("Parsing " + source);
			r.parse(source);
			}
		}

	}
