package com.sa.metadata;



public enum FieldEditor {
	Label("label"),

	Button("linkbutton"),

	Text("textbox"),

	Password("passwordbox"),

	Number("numberbox"),

	Combo("combobox"),

	CheckBox("check"),

	Date("datebox"),

	Datetime("datetimebox"),

	Textarea("textarea");

	private String editor;

	FieldEditor(String editor){
		this.editor = editor;
	}

	public String getEditor() {
		return editor;
	}
}
