package com.sa.metadata;

import java.io.Serializable;


public interface ValuePair<T> extends Serializable {

	public String getText();


	public void setText(String name);


	public T getValue();


	public void setValue(T value);

}

