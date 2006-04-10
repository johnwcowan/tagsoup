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
// The TagSoup command line UI

package org.ccil.cowan.tagsoup;
import java.util.HashMap;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.LexicalHandler;


/**
The stand-alone TagSoup program.
**/
public class CommandLine {

	// Main method: processes specified files or stdin
	// -Dfiles=true writes output to separate files with .xhtml extension
	// -Dnons suppresses namespaces, -Dnobogons suppresses unknown elements
	// -Dany makes bogons ANY rather than EMPTY,
	// -Dpyx=true, -Dhtml=true uses Pyx or HTML output
	// -Dpyxin=true uses Pyx input

	public static void main(String[] argv) throws IOException, SAXException {
		HTMLSchema s = HTMLSchema.sharedSchema();
		if (Boolean.getBoolean("nocdata")) {
			ElementType script = s.getElementType("script");
			script.setFlags(0);
			ElementType style = s.getElementType("style");
			style.setFlags(0);
			}
		if (argv.length == 0) {
			process("", System.out);
			}
		else if (Boolean.getBoolean("files")) {
			for (int i = 0; i < argv.length; i++) {
				String src = argv[i];
				String dst;
				int j = src.lastIndexOf('.');
				if (j == -1)
					dst = src + ".xhtml";
				else if (src.endsWith(".xhtml"))
					dst = src + "_";
				else
					dst = src.substring(0, j) + ".xhtml";
				System.err.println("src: " + src + " dst: " + dst);
				OutputStream os = new FileOutputStream(dst);
				process(src, os);
				}
			}
		else {
			for (int i = 0; i < argv.length; i++) {
				System.err.println("src: " + argv[i]);
				process(argv[i], System.out);
				}
			}
		}

	private static Parser myParser = null;

	private static void process(String src, OutputStream os)
			throws IOException, SAXException {
		XMLReader r;
		if (Boolean.getBoolean("reuse")) {
			if (myParser == null) myParser = new Parser();
			r = myParser;
			}
		else {
			r = new Parser();
			}

		if (Boolean.getBoolean("nons")) {
			r.setFeature(Parser.namespacesFeature, false);
			r.setFeature(Parser.namespacePrefixesFeature, false);
			}

		if (Boolean.getBoolean("nobogons")) {
			r.setFeature(Parser.ignoreBogonsFeature, true);
			}
		if (Boolean.getBoolean("any")) {
			r.setFeature(Parser.bogonsEmptyFeature, false);
			}

		if (Boolean.getBoolean("pyxin")) {
			r.setProperty(Parser.scannerProperty, new PYXScanner());
			}

		Writer w = new OutputStreamWriter(os, "UTF-8");
		ContentHandler h = chooseContentHandler(w);
		r.setContentHandler(h);
		if (Boolean.getBoolean("lexical") && h instanceof LexicalHandler) {
			r.setProperty(Parser.lexicalHandlerProperty, h);
			}
		if (src != "") {
			r.parse(src);
			}
		else {
			r.parse(new InputSource(System.in));
			}
		}

	private static ContentHandler chooseContentHandler(Writer w) {
		ContentHandler h;
		if (Boolean.getBoolean("pyx")) {
			h = new PYXWriter(w);
			}
		else if (Boolean.getBoolean("html")) {
			XMLWriter x = new XMLWriter(w);
			x.setHTMLMode(true);
			h = x;
			}
		else {
			h = new XMLWriter(w);
			}
		return h;
		}

	}
