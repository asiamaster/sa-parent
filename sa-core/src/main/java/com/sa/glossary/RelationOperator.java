package com.sa.glossary;


public enum RelationOperator {
	Equal("=","等于"),
	NotEqual("!=","不等于"),
	GreatThan(">","大于"),
	GreatEqualThan(">=","大于等于"),
	LittleThan("<","小于"),
	LittleEqualThan("<=","小于等于"),
	Match("like","匹配"),
	NotMatch("not like","不匹配"),
	Is("is","是"),
	IsNot("is not","非");

	private String value;
	private String text;

	RelationOperator(String value, String text){
		this.value = value;
		this.text = text;
	}

	public String getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
