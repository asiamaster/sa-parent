package com.sa.dto;

import java.io.Serializable;
import java.util.*;


public class DTOList<T extends IDTO> extends AbstractList<T> implements List<T>, IPagingResult, RandomAccess, Cloneable, Serializable {
	private static final long serialVersionUID = 6003648687822621383L;

	@SuppressWarnings("unchecked")
	private List resDatas;

	private Class<T> dtoClazz;

	private int rowCount;


	public DTOList(Class<T> dtoClazz, List<? extends DTO> resDatas) {
		this.resDatas = resDatas;
		this.dtoClazz = dtoClazz;
	}


	public DTOList(Class<T> dtoClazz, int size) {
		this.dtoClazz = dtoClazz;
		if (size == 0)
			this.resDatas = Collections.EMPTY_LIST;
		else
			this.resDatas = new ArrayList<T>(size);
	}


	public DTOList(Class<T> dtoClazz) {
		this.dtoClazz = dtoClazz;
		this.resDatas = new ArrayList<T>();
	}


	public int getRowCount() {
		return rowCount;
	}


	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}


	@Override
	public int size() {
		return resDatas.size();
	}


	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		Object obj = resDatas.get(index);
		T retval = null;

		if (obj != null) {
			if (obj instanceof DTO) {
				retval = DTOUtils.internalAs(obj, dtoClazz);
				resDatas.set(index, retval);
			}  else {
				retval = (T) obj;
			}
		}
		return retval;
	}


	@Override
	public Object[] toArray() {
		Object[] retval = new Object[resDatas.size()];
		for (int i = 0; i < resDatas.size(); i++) {
			retval[i] = get(i);
		}
		return retval;
	}


	@SuppressWarnings( { "unchecked", "hiding" })
	@Override
	public <T> T[] toArray(T[] a) {
		int size = resDatas.size();
		if (a.length < size)
			a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		Object[] array = toArray();
		System.arraycopy(array, 0, a, 0, size);
		if (a.length > size)
			a[size] = null;
		return a;
	}


	@SuppressWarnings("unchecked")
	public void trimToSize() {
		if (resDatas instanceof ArrayList)
			((ArrayList) resDatas).trimToSize();
	}


	@SuppressWarnings("unchecked")
	@Override
	public void add(int index, T element) {
		resDatas.add(index, element);
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean add(T o) {
		return resDatas.add(o);
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends T> c) {
		return resDatas.addAll(c);
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return resDatas.addAll(index, c);
	}


	public void clear() {
		resDatas.clear();
	}


	@SuppressWarnings("unchecked")
	public T remove(int index) {
		return object2T(resDatas.remove(index));
	}


	public boolean remove(Object o) {
		return resDatas.remove(o);
	}


	@SuppressWarnings("unchecked")
	@Override
	public T set(int index, T element) {
		return object2T(resDatas.set(index, element));
	}


	@Override
	public String toString() {
		return resDatas.toString();
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	@SuppressWarnings("unchecked")
	private T object2T(Object obj) {
		T retval = null;
		if (obj != null) {
			if (obj instanceof DTO) {
				retval = DTOUtils.internalAs(obj, dtoClazz);
			} else {
				retval = (T) obj;
			}
		}
		return retval;
	}

}
