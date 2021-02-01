package com.sa.beetl.tag;

import com.alibaba.fastjson.JSONObject;
import com.sa.metadata.FieldEditor;
import org.beetl.core.tag.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("fieldEditor")
@ConditionalOnExpression("'${beetl.enable}'=='true'")
public class FieldEditorTag extends Tag {
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String TAB = "    ";


	private final String VAR_NAME_FIELD = "varName";

	@Override
	public void render() {
		try {

			Map<String, Object> argsMap = (Map) this.args[1];
			String varName = (String) argsMap.get(VAR_NAME_FIELD);
			varName = varName == null ? "fieldEditor" : varName;
			JSONObject jsonObject = new JSONObject();
			for(FieldEditor fieldEditor : FieldEditor.values()){
				jsonObject.put(fieldEditor.name(), fieldEditor.getEditor());
			}
			ctx.byteWriter.writeString("var " + varName + "=" + jsonObject.toJSONString() + ";" + LINE_SEPARATOR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
