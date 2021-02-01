package com.sa.beetl;

import org.beetl.core.Context;
import org.beetl.core.statement.Statement;
import org.beetl.core.tag.HTMLTagVarBindingWrapper;
import org.beetl.core.tag.Tag;

import java.util.LinkedHashMap;


public class HTMLVarBindingTag extends HTMLTagVarBindingWrapper
{

	Tag tag = new HTMLTag();

	@Override
	public void render()
	{
		tag.render();

	}






	@Override
	public void mapName2Index(LinkedHashMap<String, Integer> map)
	{
		((HTMLTag)tag).setBinds(map);
	}

	@Override
	public void init(Context ctx, Object[] args, Statement st)
	{
		tag.init(ctx, args, st);
	}

}
