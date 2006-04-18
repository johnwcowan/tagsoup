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
import java.util.Hashtable;
import java.util.Enumeration;
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

	static Hashtable options = new Hashtable(); static {
		options.put("--nocdata", Boolean.FALSE); // CDATA elements are normal
		options.put("--files", Boolean.FALSE);	// process arguments as separate files
		options.put("--reuse", Boolean.FALSE);	// reuse a single Parser
		options.put("--nons", Boolean.FALSE);	// no namespaces
		options.put("--nobogons", Boolean.FALSE);  // suppress unknown elements
		options.put("--any", Boolean.FALSE);	// unknowns have ANY content model
		options.put("--pyxin", Boolean.FALSE);	// input is PYX
		options.put("--lexical", Boolean.FALSE); // output comments
		options.put("--pyx", Boolean.FALSE);	// output is PYX
		options.put("--html", Boolean.FALSE);	// output is HTML
		options.put("--method=", Boolean.FALSE); // output method
		options.put("--omit-xml-declaration", Boolean.FALSE);
							// omit XML decl
		options.put("--encoding=", Boolean.FALSE); // specify encoding
		options.put("--help", Boolean.FALSE); 	// display help
		}

	/**
	Main method.  Processes specified files or standard input.
	**/

	public static void main(String[] argv) throws IOException, SAXException {
		HTMLSchema s = HTMLSchema.sharedSchema();
		int optind = getopts(options, argv);
		if (hasOption(options, "--help")) {
			doHelp();
			return;
			}
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

	// Print the help message

	private static void doHelp() {
		System.err.print("usage: java -jar tagsoup-*.jar ");
		System.err.print(" [ ");
		boolean first = true;
		for (Enumeration e = options.keys(); e.hasMoreElements(); ) {
			if (!first) {
				System.err.print("| ");
				}
			first = false;
			String key = (String)(e.nextElement());
			System.err.print(key);
			if (key.endsWith("="))
				System.err.print("?");
				System.err.print(" ");
			}
		System.err.println("]*");
	}

	private static Parser myParser = null;

	// Process one source onto an output stream.

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
		if (hasOption(options, "--encoding=")) {
//			System.out.println("%% Found --encoding");
			String encoding = (String)options.get("--encoding=");
			if (encoding != null) s.setEncoding(encoding);
			}
		r.parse(s);
		}

	// Pick a content handler to generate the desired format.

	private static ContentHandler chooseContentHandler(Writer w) {
		ContentHandler h;
		if (hasOption(options, "--pyx")) {
			h = new PYXWriter(w);
			}
		else if (hasOption(options, "--html")) {
			XMLWriter x = new XMLWriter(w);
			x.setOutputProperty(XMLWriter.METHOD, "html");
			x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
			h = x;
			}
		else if (hasOption(options, "--method=")) {
//			System.out.println("%% Found --method=");
			XMLWriter x = new XMLWriter(w);
			String method = (String)options.get("--method=");
			if (method != null) {
//				System.out.println("%% method=[" + method + "]");
				x.setOutputProperty(XMLWriter.METHOD, method);
				}
			h = x;
			}
		else if (hasOption(options, "--omit-xml-declaration")) {
//			System.out.println("%% Found --omit-xml-declaration");
			XMLWriter x = new XMLWriter(w);
			x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
			h = x;
			}
		else {
			h = new XMLWriter(w);
			}
		return h;
		}

	// Options processing

	private static int getopts(Hashtable options, String[] argv) {
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
				if (value == null) options.put(arg, Boolean.TRUE);
				else options.put(arg, value);
//				System.out.println("%% Parsed [" + arg + "]=[" + value + "]");
				}
			else {
				System.err.print("Unknown option ");
				System.err.println(arg);
				System.exit(1);
				}
			}
		return optind;
		}

	// Return true if an option exists.

	private static boolean hasOption(Hashtable options, String option) {
		if (Boolean.getBoolean(option)) return true;
		else if (options.get(option) != Boolean.FALSE) return true;
		return false;
		}

	}
