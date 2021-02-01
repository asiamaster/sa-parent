package com.sa.metadata;

import com.sa.metadata.annotation.EditMode;
import com.sa.metadata.annotation.FieldDef;
import com.sa.util.POJOUtils;
import com.sa.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MetadataUtils {
	protected static final Logger logger = LoggerFactory.getLogger(MetadataUtils.class);

	private static final HashMap<String, ObjectMeta> buffer = new HashMap<String, ObjectMeta>();


	public static void clearDTOMetas() {
		buffer.clear();
	}


	public static ObjectMeta getDTOMeta(Class<?> dtoClazz) {
		assert (dtoClazz != null);
		ObjectMeta retval = buffer.get(dtoClazz.getName());
		if (retval == null) {
			retval = new ObjectMeta();
			retval.setLoadSuper(true);
			retval.addAll(getDTOMetaByMethod(dtoClazz));
			Collections.sort(retval);
			buffer.put(dtoClazz.getName(), retval);
		}
		if (!retval.isLoadSuper()) {
			loadSuperMeta(dtoClazz, retval);
		}
		return retval;
	}


	private static boolean hasFieldMeta(ObjectMeta subMeta, FieldMeta fieldMeta) {
		for (FieldMeta meta : subMeta) {
			if (meta.getName().equals(fieldMeta.getName()))
				return true;
		}
		return false;
	}


	private static void addSuperMeta(ObjectMeta subMeta, ObjectMeta superMeta) {
		for (FieldMeta fieldMeta : superMeta) {
			if (!hasFieldMeta(subMeta, fieldMeta)) {
				subMeta.add(fieldMeta);
			}
		}
	}

	private static void loadMeta(Class<?> dtoClz, ObjectMeta subMeta) {
		ObjectMeta superMeta = buffer.get(dtoClz.getName());
		if (superMeta == null) {
			superMeta = new ObjectMeta();
		}
		if (!superMeta.isLoadSuper()) {
			loadSuperMeta(dtoClz, superMeta);
		}
		addSuperMeta(subMeta, superMeta);
	}

	private static void loadSuperMeta(Class<?> dtoClazz, ObjectMeta objectMeta) {

		Class<?> superClz = dtoClazz.getSuperclass();
		if (superClz != null) {
			loadMeta(superClz, objectMeta);
		}

		Class<?>[] interfaces = dtoClazz.getInterfaces();
		if (interfaces != null && interfaces.length > 0) {
			for (Class<?> superInterface : interfaces) {
				loadMeta(superInterface, objectMeta);
			}
		}
		Collections.sort(objectMeta);
		objectMeta.setLoadSuper(true);
		buffer.put(dtoClazz.getName(), objectMeta);
	}


	@SuppressWarnings("unchecked")
	private static FieldMeta newFieldMetaFromGetMethod(Class<?> dtoClazz, Method method) {
		FieldMeta retval = null;
		FieldDef fieldDef = method.getAnnotation(FieldDef.class);
		if (fieldDef != null) {
			retval = newFieldMetaFromFieldDef(POJOUtils.getBeanField(method
					.getName()), fieldDef);
		}

		if (retval != null) {
			retval.setType(method.getReturnType());
			updateFieldMetaByEditMode(retval, method);
			updateFieldMetaFromField(retval, method, dtoClazz);
		}
		return retval;
	}

	private static void updateFieldMetaFromField(FieldMeta fMeta, Method method, Class<?> dtoClazz) {
		String fieldName = POJOUtils.getBeanField(method);
		Field field = null;
		if(!dtoClazz.isInterface()) {
			field = ReflectionUtils.getAccessibleField(dtoClazz, fieldName);
		}
		String dbFieldName = null;
		Column column = null;


        if(field == null){
	        column = method.getAnnotation(Column.class);
        }else{
            column = field.getAnnotation(Column.class);
        }

		if(column != null) {
			dbFieldName = column.name();
		}else{
			dbFieldName = POJOUtils.humpToLineFast(fieldName);
		}
		fMeta.setColumn(dbFieldName);
	}


	private static FieldMeta newFieldMetaFromFieldDef(String field, FieldDef fieldDef) {
		FieldMeta retval = new FieldMeta(field);

		retval.setLabel((fieldDef.label() == null && fieldDef.label().length() == 0) ? field
				: fieldDef.label());
		retval.setLength(fieldDef.maxLength());
		retval.setDefValue(fieldDef.defValue());
		return retval;
	}


	private static void updateFieldMetaByEditMode(FieldMeta fMeta, Method method) {
		EditMode editMode = method.getAnnotation(EditMode.class);
		if (editMode != null) {
			fMeta.setRequired(editMode.required());
			fMeta.setVisible(editMode.visible());
			fMeta.setReadonly(editMode.readOnly());
			fMeta.setEditor(editMode.editor());
			fMeta.setParams(editMode.params());
			fMeta.setIndex(editMode.index());
			fMeta.setSortable(editMode.sortable());
			fMeta.setFormable(editMode.formable());
			fMeta.setGridable(editMode.gridable());
			fMeta.setQueryable(editMode.queryable());

			if (StringUtils.isNotBlank(editMode.txtField())) {
				fMeta.setTxtField(editMode.txtField());
			}

			fMeta.setProvider(editMode.provider());
		}
	}

	private static List<FieldMeta> getDTOMetaByMethod(Class<?> dtoClazz) {
		List<FieldMeta> retval = new ArrayList<>();
		Method[] methods = dtoClazz.getMethods();
		if (methods != null) {
			for (Method method : methods) {
				if (POJOUtils.isGetMethod(method)
						|| POJOUtils.isISMethod(method)) {
					FieldMeta fieldMeta = newFieldMetaFromGetMethod(dtoClazz, method);
					if (fieldMeta != null) {
						retval.add(fieldMeta);
					}
				}
			}
		}
		return retval;
	}

}
