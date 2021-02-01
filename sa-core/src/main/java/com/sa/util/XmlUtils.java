package com.sa.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class XmlUtils {
	protected static final Logger log = LoggerFactory.getLogger(XmlUtils.class);


	public static Document File2Document(String path) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			log.error(e.getMessage());
		}
		return document;
	}


	public static void Document2File(Document doc,
	                                 String path) {
		OutputFormat format = new OutputFormat("", true);
		format.setEncoding("UTF-8");
		try {
			XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(path), format);
			xmlWriter.write(doc);
			xmlWriter.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}



	public static String Document2String(Document document) {
		String s = "";


		ByteArrayOutputStream out = new ByteArrayOutputStream();

		OutputFormat format = new OutputFormat("", true, "UTF-8");
		try {
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(document);
			s = out.toString("UTF-8");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return s;
	}


	public static Document String2Document(String xml) {
		try {
			return DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			log.error(e.getMessage());
		}
		return null;
	}























}
