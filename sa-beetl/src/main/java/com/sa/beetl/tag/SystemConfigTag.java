package com.sa.beetl.tag;

import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.BodyContent;
import org.beetl.core.tag.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("config")
@ConditionalOnExpression("'${beetl.enable}'=='true'")
public class SystemConfigTag extends Tag {


	private final String NAME_FIELD = "name";

	private final String DEFAULT_VALUE_FIELD = "defValue";

	private final String HAS_FIELD = "has";

	@Override
	public void render() {
		try {
			Map<String, Object> argsMap = (Map) this.args[1];
			String has = (String) argsMap.get(HAS_FIELD);

			if(StringUtils.isNotBlank(has)){
				BodyContent content = getBodyContent();
				if(StringUtils.isNotBlank(SpringUtil.getProperty(has))){
					ctx.byteWriter.writeString(content.getBody());
				}
			}else{
				String name = (String) argsMap.get(NAME_FIELD);
				if(null == name) {
					return;
				}
				String defaultValue = (String) argsMap.get(DEFAULT_VALUE_FIELD);
				String value = defaultValue == null ? SpringUtil.getProperty(name) : SpringUtil.getProperty(name, defaultValue);
				ctx.byteWriter.writeString(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
