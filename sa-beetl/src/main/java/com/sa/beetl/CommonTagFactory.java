package com.sa.beetl;

import org.beetl.core.tag.Tag;
import org.beetl.core.tag.TagFactory;


public class CommonTagFactory implements TagFactory {

    private Tag tag;
    public CommonTagFactory(Tag tag){
        this.tag = tag;
    }

    @Override
    public Tag createTag(){
        return tag;
    }
}
