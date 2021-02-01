package com.sa.beetl;

import java.util.ArrayList;
import java.util.List;

public class TagChildrenContext {
		private List<HTMLTag> children = null;

	public List<HTMLTag> getChildren() {
		if(children==null) children = new ArrayList<HTMLTag>();
		return children;
	}
	public void setChildren(List<HTMLTag> children) {
		this.children = children;
	}
	
}
