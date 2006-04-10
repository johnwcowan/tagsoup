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

	static HashMap options = new HashMap(); static {
		options.put("--nocdata", null);	// CDATA elements are normal
		options.put("--files", null);	// process arguments as separate files
		options.put("--reuse", null);	// reuse a single Parser
		options.put("--nons", null);	// no namespaces
		options.put("--nobogons", null);  // suppress unknown elements
		options.put("--any", null);	// unknowns have ANY content model
		options.put("--pyxin", null);	// input is PYX
		options.put("--lexical", null);	// output comments
		options.put("--pyx", null);	// output is PYX
		options.put("--html", null);	// output is HTML
		options.put("--encoding=", null);	// specify encoding
		}

	// Main method: processes specified files or stdin

	public static void main(String[] argv) throws IOException, SAXException {
		HTMLSchema s = HTMLSchema.sharedSchema();
		int optind = getopts(options, argv);
		if (hasOption(options, "--nocdata")) {
			ElementType script = s.getElementType("script");
			script.setFlags(0);
			ElementType style = s.getElementType("style");
			style.setFlags(0);
			}
		if (argv.length == optind) {
			process("", System.out);
			}
		else if (hasOption(options, "--files")) {
			for (int i = optind; i < argv.length; i++) {
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
			for (int i = optind; i < argv.length; i++) {
				System.err.println("src: " + argv[i]);
				process(argv[i], System.out);
				}
			}
		}

	private static Parser myParser = null;

	private static void process(String src, OutputStream os)
			throws IOException, SAXException {
		XMLReader r;
		if (hasOption(options, "--reuse")) {
			if (myParser == null) myParser = new Parser();
			r = myParser;
			}
		else {
			r = new Parser();
			}

		if (hasOption(options, "--nons")) {
			r.setFeature(Parser.namespacesFeature, false);
			r.setFeature(Parser.namespacePrefixesFeature, false);
			}

		if (hasOption(options, "--nobogons")) {
			r.setFeature(Parser.ignoreBogonsFeature, true);
			}
		if (hasOption(options, "--any")) {
			r.setFeature(Parser.bogonsEmptyFeature, false);
			}

		if (hasOption(options, "--pyxin")) {
			r.setProperty(Parser.scannerProperty, new PYXScanner());
			}

		Writer w = new OutputStreamWriter(os, "UTF-8");
		ContentHandler h = chooseContentHandler(w);
		r.setContentHandler(h);
		if (hasOption(options, "--lexical") && h instanceof LexicalHandler) {
			r.setProperty(Parser.lexicalHandlerProperty, h);
			}
		InputSource s = new InputSource();
		if (src != "") {
			s.setSystemId(src);
			}
		else {
			s.setByteStream(System.in);
			}
		String encoding = (String)options.get("--encoding=");
		if (encoding != null) s.setEncoding(encoding);
		r.parse(s);
		}

	private static ContentHandler chooseContentHandler(Writer w) {
		ContentHandler h;
		if (hasOption(options, "--pyx")) {
			h = new PYXWriter(w);
			}
		else if (hasOption(options, "--html")) {
			XMLWriter x = new XMLWriter(w);
			x.setHTMLMode(true);
			h = x;
			}
		else {
			h = new XMLWriter(w);
			}
		return h;
		}

	private static int getopts(HashMap options, String[] argv) {
		int optind;
		for (optind = 0; optind < argv.length; optind++) {
			String arg = argv[optind];
			String value = null;
			if (arg.charAt(0) != '-') break;
			int eqsign = arg.indexOf('=');
			if (eqsign != -1) {
				value = arg.substring(eqsign + 1, arg.length());
				arg = arg.substring(0, eqsign + 1);
				}
			if (options.containsKey(arg)) {
				if (value == null) {
					options.put(arg, Boolean.TRUE);
					}
				else {
					options.put(arg, value);
					}
				}
			else {
				System.err.print("Unknown option ");
				System.err.println(arg);
				System.exit(1);
				}
			}
		return optind;
		}

	private static boolean hasOption(HashMap options, String option) {
		if (Boolean.getBoolean(option)) return true;
		else if (options.get(option) == Boolean.TRUE) return true;
		return false;
		}

	}
