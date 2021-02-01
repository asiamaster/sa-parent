package com.sa.beetl;


import java.io.IOException;
import java.io.Writer;

public class NoLockEmptyStringWriter extends Writer
{

	

	public NoLockEmptyStringWriter()
	{
		
	}

	public NoLockEmptyStringWriter(char buf[])
	{
		
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		
	}

	@Override
	public void write(String str) throws IOException
	{
		
	}

	@Override
	public void flush() throws IOException
	{

	}

	@Override
	public void close() throws IOException
	{


	}

	@Override
	public String toString()
	{
		return "";
	}
}