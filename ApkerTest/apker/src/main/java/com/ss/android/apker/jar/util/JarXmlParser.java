package com.ss.android.apker.jar.util;

import android.content.Context;

import com.ss.android.apker.entity.ApkEntity;
import com.ss.android.apker.jar.common.Constant;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class JarXmlParser {
	private Context context;
	
	public JarXmlParser(Context context) {
		this.context = context;
	}
	
	public ArrayList<ApkEntity> parseJars(){
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			JarHandler handler = new JarHandler();
			parser.parse(context.getResources().getAssets().open(Constant.JAR_IN_MAIN_NAME+"/"+Constant.JARCONFIG_IN_MAIN_NAME), handler);
			return handler.getList();
		} catch (Exception e) {
			return null;
		}
	}
	
	private class JarHandler extends DefaultHandler{
		private final int JAR_NAME = 1;			//插件名称
		private final int JAR_PACKAGENAME = 2;	//插件包名
		private final int JAR_TYPE = 3;			//插件类型
		private final int JAR_VERSION = 4;		//插件版本
		private final int JAR_TITLE = 5;		//插件标题
		private final int JAR_DESC = 6;			//插件描述
		private final int JAR_STATUS = 7;		//插件状态 0内置，1服务端
		private int currentstate = 0;
		private ArrayList<ApkEntity> jars;
		private ApkEntity entity;
		
		public ArrayList<ApkEntity> getList(){
			return jars;
		}
		
		@Override
		public void startDocument() throws SAXException {
			jars = new ArrayList<ApkEntity>();
		}
		
		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(localName.equals("jars")){
				currentstate = 0;
				return;
			}
			if(localName.equals("item")){
				entity = new ApkEntity();
				return;
			}
			if(localName.equals("name")){
				currentstate = JAR_NAME;
				return;
			}
			if(localName.equals("packagename")){
				currentstate = JAR_PACKAGENAME;
				return;
			}
			if(localName.equals("type")){
				currentstate = JAR_TYPE;
				return;
			}
			if(localName.equals("version")){
				currentstate = JAR_VERSION;
				return;
			}
			if(localName.equals("title")){
				currentstate = JAR_TITLE;
				return;
			}
			if(localName.equals("desc")){
				currentstate = JAR_DESC;
				return;
			}
			if(localName.equals("status")){
				currentstate = JAR_STATUS;
				return;
			}
			currentstate = 0;
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if(localName.equals("item")){
				if(jars != null){
					jars.add(entity);
				}
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String theString = new String(ch, start, length);
			switch (currentstate) {
			case JAR_NAME:
				entity.name = theString;
				currentstate = 0;
				break;
			case JAR_PACKAGENAME:
				entity.packageName = theString;
				currentstate = 0;
				break;
			case JAR_TYPE:
				entity.type = Integer.parseInt(theString);
				currentstate = 0;
				break;
			case JAR_VERSION:
				entity.version = Integer.parseInt(theString);
				currentstate = 0;
				break;
			case JAR_TITLE:
				entity.title = theString;
				currentstate = 0;
				break;
			case JAR_DESC:
				entity.desc = theString;
				currentstate = 0;
				break;
			case JAR_STATUS:
				entity.status = Integer.valueOf(theString);
				currentstate = 0;
				break;
			}
		}
	}
}
