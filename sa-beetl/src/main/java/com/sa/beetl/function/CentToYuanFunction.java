package com.sa.beetl.function;

import com.sa.util.MoneyUtils;
import org.beetl.core.Context;
import org.beetl.core.Function;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;


@Component("centToYuan")
@ConditionalOnExpression("'${beetl.enable}'=='true'")
public class CentToYuanFunction implements Function {
    @Override
    public Object call(Object[] objects, Context context) {
        Object o = objects[0];
        if (o != null)
        {
            try
            {
                context.byteWriter.writeString(MoneyUtils.centToYuan(Long.parseLong(o.toString())));
            }
            catch (Exception e)
            {
                return "分转元失败,参数:"+o;
            }
        }
        return "";
    }
}
