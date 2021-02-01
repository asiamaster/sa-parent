package com.sa.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ObjectMeta extends ArrayList<FieldMeta>{
	private static final long serialVersionUID = 5357724772449503613L;

	private boolean loadSuper = false;
	
	public ObjectMeta() {
		super();
	}

	public ObjectMeta(Collection<? extends FieldMeta> c) {
		super(c);
	}


	public FieldMeta getFieldMetaById(String id) {
		Iterator<FieldMeta> it = iterator();
		for (; it.hasNext();) {
			FieldMeta fieldMeta = it.next();
			if (fieldMeta.getName().equals(id))
				return fieldMeta;
		}
		return null;
	}

	public boolean isLoadSuper() {
		return loadSuper;
	}

	public void setLoadSuper(boolean loadSuper) {
		this.loadSuper = loadSuper;
	}

}
