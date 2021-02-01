package com.sa.metadata;


public class ValuePairImpl<T> implements ValuePair<T> {
	private static final long serialVersionUID = -7249602894129329260L;


	public static final ValuePairImpl<Object> EMPTY = new ValuePairImpl<Object>(null, null);


	private String text;


	private T value;

	public ValuePairImpl() {
	}


	public ValuePairImpl(String name, T value) {
		this.text = name;
		this.value = value;
	}
	@Override
	public String getText() {
		return text;
	}
	@Override
	public T getValue() {
		return value;
	}
	@Override
	public void setText(String name) {
		this.text = name;
	}
	@Override
	public void setValue(T value) {
		this.value = value;
	}

}

