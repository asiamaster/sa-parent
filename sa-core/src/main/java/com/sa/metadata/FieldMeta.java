package com.sa.metadata;


import java.io.Serializable;


public class FieldMeta implements Comparable<FieldMeta>, Serializable, Cloneable {
	private static final long serialVersionUID = -2710206113310782917L;

	String name;
	String label;
	int length;
	String defValue;
	Class<?> type;

	String provider;
	String txtField;

	boolean required;
	boolean visible = true;
	boolean readonly;
	FieldEditor editor;
	String params;

	boolean sortable;
	boolean formable;
	boolean gridable;
	boolean queryable = true;
	String column;

	int index = Integer.MAX_VALUE;

	public FieldMeta(String name) {
		this.name = name;
		this.label = name;
	}


	@Override
	public int compareTo(FieldMeta o) {
		return index - o.getIndex();
	}

	@Override
	public Object clone() {
		try {
			return (FieldMeta) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getDefValue() {
		return defValue;
	}

	public void setDefValue(String defValue) {
		this.defValue = defValue;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getTxtField() {
		return txtField;
	}

	public void setTxtField(String txtField) {
		this.txtField = txtField;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public FieldEditor getEditor() {
		return editor;
	}

	public void setEditor(FieldEditor editor) {
		this.editor = editor;
	}

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public boolean isFormable() {
		return formable;
	}

	public void setFormable(boolean formable) {
		this.formable = formable;
	}

	public boolean isGridable() {
		return gridable;
	}

	public void setGridable(boolean gridable) {
		this.gridable = gridable;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isQueryable() {
		return queryable;
	}

	public void setQueryable(boolean queryable) {
		this.queryable = queryable;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
}
