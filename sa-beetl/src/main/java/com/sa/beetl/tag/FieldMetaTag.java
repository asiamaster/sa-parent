package com.sa.beetl.tag;

import com.alibaba.fastjson.JSONObject;
import com.sa.metadata.FieldMeta;
import com.sa.metadata.MetadataUtils;
import com.sa.metadata.ObjectMeta;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.tag.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;


@Component("fieldMeta")
@ConditionalOnExpression("'${beetl.enable}'=='true'")
public class FieldMetaTag extends Tag {
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String TAB = "    ";


	private final String DTO_CLASS_FIELD = "dtoClass";
	private final String VAR_NAME_FIELD = "varName";

	@Override
	public void render() {
		try {

			Map<String, Object> argsMap = (Map)this.args[1];
			String dtoClass = (String) argsMap.get(DTO_CLASS_FIELD);
			String varName = (String) argsMap.get(VAR_NAME_FIELD);

			if(StringUtils.isBlank(dtoClass)) {
				return;
			}
			Class clazz = Class.forName(dtoClass);
			varName = varName == null ? getVarName(clazz.getSimpleName()) : varName;
			ObjectMeta objectMeta = MetadataUtils.getDTOMeta(clazz);
			JSONObject jsonObject = new JSONObject();
			for(FieldMeta fieldMeta : objectMeta){
				jsonObject.put(fieldMeta.getColumn(), JSONObject.toJSON(fieldMeta));
			}
			ctx.byteWriter.writeString("var " + varName + " = " + jsonObject.toJSONString()+";"+LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	public static String getVarName(String clazzFullName){
		Assert.hasText(clazzFullName, "clazzFullName不能为空");
		return lowerCaseFirstChar(clazzFullName.substring(clazzFullName.lastIndexOf(".")+1))+"Meta";
	}


	private static String lowerCaseFirstChar(String value){
		return String.valueOf(value.charAt(0)).toLowerCase() + value.substring(1);
	}


}
