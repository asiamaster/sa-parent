package com.sa.beetl.tag;

import org.beetl.core.tag.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component("null")
@ConditionalOnExpression("'${beetl.enable}'=='true'")
public class NullTag extends Tag {
    @Override
    public void render() {







        try{
            this.ctx.byteWriter.writeString("");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
