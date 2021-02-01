package com.sa.activiti.boot;

import com.sa.gid.generator.GSN;
import org.activiti.engine.impl.cfg.IdGenerator;


public class IdGen implements IdGenerator{

    private GSN gsn;

    public IdGen(GSN gsn){
        this.gsn = gsn;
    }






    @Override
    public String getNextId() {

        return gsn.next();
    }

}
