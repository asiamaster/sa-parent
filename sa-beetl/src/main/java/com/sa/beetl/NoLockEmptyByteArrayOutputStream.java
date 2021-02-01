package com.sa.beetl;


import java.io.OutputStream;

public class NoLockEmptyByteArrayOutputStream extends OutputStream
{


	public NoLockEmptyByteArrayOutputStream()
	{
		
	}

	public NoLockEmptyByteArrayOutputStream(int size)
	{

		
	}

	@Override
	public void write(int b)
	{
		
	}

	@Override
	public void write(byte b[], int off, int len)
	{

	
	}

	

	public void reset()
	{
		
	}

	public byte toByteArray()[]
	{
		return new byte[0];
	}

	public int size()
	{
		return 0;
	}

}