package com.sa.activiti.util;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti.validation.ValidationError;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;


public class ActivitiUtils {

    
    public static BpmnModel toBpmnModel(InputStream is) throws XMLStreamException {

        BpmnXMLConverter converter = new BpmnXMLConverter();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(is);

        return converter.convertToBpmnModel(reader);
    }

    
    public static BpmnModel toBpmnModel(String bpmnXml) throws XMLStreamException {

        BpmnXMLConverter converter = new BpmnXMLConverter();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(bpmnXml.getBytes()));

        return converter.convertToBpmnModel(reader);
    }

    
    public static String validateBpmnModel(BpmnModel bpmnModel) {

        ProcessValidatorFactory processValidatorFactory = new ProcessValidatorFactory();
        ProcessValidator defaultProcessValidator = processValidatorFactory.createDefaultProcessValidator();

        List<ValidationError> validate = defaultProcessValidator.validate(bpmnModel);
        return validate.isEmpty() ? null : validate.get(0).getProblem();
    }
}
