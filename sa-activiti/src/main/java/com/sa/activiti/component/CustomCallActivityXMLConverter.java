package com.sa.activiti.component;

import org.activiti.bpmn.converter.CallActivityXMLConverter;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;

import javax.xml.stream.XMLStreamReader;


public class CustomCallActivityXMLConverter extends CallActivityXMLConverter {

    @Override
    protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
        CallActivity callActivity = new CallActivity();
        BpmnXMLUtil.addXMLLocation(callActivity, xtr);
        callActivity.setCalledElement(xtr.getAttributeValue(null, ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT));
        callActivity.setInheritVariables(Boolean.TRUE);
        parseChildElements(getXMLElementName(), callActivity, childParserMap, model, xtr);
        return callActivity;
    }
}
