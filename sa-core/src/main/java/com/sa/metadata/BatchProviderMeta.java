package com.sa.metadata;

import com.sa.dto.IDTO;

import java.util.Map;
import java.util.function.Function;


public interface BatchProviderMeta extends IDTO {

    Boolean getIgnoreCaseToRef();
    void setIgnoreCaseToRef(Boolean ignoreCaseToRef);


    Map<String, String> getEscapeFileds();
    void setEscapeFileds(Map<String, String> escapeFileds);


    String getEscapeFiled();
    void setEscapeFiled(String escapeFiled);


    String getRelationTablePkField();
    void setRelationTablePkField(String relationTablePkField);



    String getFkField();
    void setFkField(String fkField);


    Function getMismatchHandler();
    void setMismatchHandler(Function mismatchHandler);
}
