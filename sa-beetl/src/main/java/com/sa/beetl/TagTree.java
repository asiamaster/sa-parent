package com.sa.beetl;

import java.util.Iterator;
import java.util.LinkedList;

public class TagTree {
	LinkedList<HTMLTag> tags = new LinkedList<HTMLTag> ();
	
	public void addTag(HTMLTag tag){
		tags.addLast(tag);
	}
	public HTMLTag getParenet(HTMLTag tag){
		HTMLTag parent = null;
		Iterator<HTMLTag> it = tags.iterator();
		while(it.hasNext()){
			HTMLTag v = it.next();
			if(v==tag){
				return parent;
			}else{
				parent = v;
			}
		}
		return null;
	}
	
	public void removeTag(HTMLTag tag){
		tags.remove(tag);
	}
}
