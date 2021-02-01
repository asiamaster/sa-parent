package com.sa.dto;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;


public class DTO extends HashMap<String, Object> {
	private static final long serialVersionUID = -514229978937800587L;


	private Map<String, Object> metadata = new HashMap<>(4);


	private boolean listenModify;



	private boolean modified;


	public void beginMonitorModify() {
		listenModify = true;
		modified = false;
	}


	public void endMonitorModify() {
		listenModify = false;
	}


	public boolean isModified() {
		return modified;
	}

	@Override
	public Object put(String key, Object value) {
		Object retval = super.put(key, value);
		if (listenModify) {
			modified = true;
		}
		return retval;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (this == o) {
			return true;
		} else if (o instanceof DTO) {
			return DTOUtils.isEquals(this, o);
		} else if (DTOUtils.isProxy(o)) {
			DTO tmp = DTOUtils.go(o);
			if (this == tmp) {
				return true;
			} else {
				return DTOUtils.isEquals(this, tmp);
			}
		}
		return false;
	}


	public DTO() {
		super();
	}


	public DTO(int initialCapacity) {
		super(initialCapacity);
	}


	@SuppressWarnings("unchecked")
	public DTO(Map m) {
		super(m);
	}


	public boolean containsMetadata(String key) {
		if(metadata != null)
			return metadata.containsKey(key);
		return false;
	}


	public Object getMetadata(String key) {
		if(metadata != null)
			return metadata.get(key);
		return null;
	}

	public Map<String, Object> getMetadata(){
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata){
		this.metadata = metadata;
	}


	public Object setMetadata(String key, Object value) {
		if (metadata == null)

			metadata = new HashMap<String, Object>(4);
		return metadata.put(key, value);
	}


	public Object removeMetadata(String key) {
		if (metadata != null)
			return metadata.remove(key);
		return null;
	}


	@Override
	public Object clone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception ex) {
		}
		return super.clone();
	}

	private int hash;
	@Override
	public int hashCode() {
		int h = hash;
		String value = toString();
		if (h == 0 && value.length() > 0) {
			char val[] = value.toCharArray();
			for (int i = 0; i < value.length(); i++) {
				h = 31 * h + val[i];
			}
			hash = h;
		}
		return h;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
