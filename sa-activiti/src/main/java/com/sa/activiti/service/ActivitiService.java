package com.sa.activiti.service;

import com.sa.activiti.dto.ActModelVO;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public interface ActivitiService extends ModelDataJsonConstants {

    @Transactional
    Model createModel(String key, String name, String description, String category) throws UnsupportedEncodingException;


    @Transactional
    Model createModel(ActModelVO actModelVO) throws UnsupportedEncodingException;


    void createModel(BpmnModel bpmnModel, String filename, String deploymentId) throws IOException, XMLStreamException;


    void saveModel(String modelId, String name, String description, String json_xml, String svg_xml);


    Deployment deployByModelId(String modelId) throws ActivitiException, IOException;


    Deployment deployFormByModelId(String modelId) throws ActivitiException, IOException;


    Model getModelById(String modelId);


    @Transactional
    void deleteModel(String modelId);


    Page<Model> getAllModels(Pageable pageable);


    @Transactional
    void deleteDeployment(String deploymentId);


    void exportModel(String modelId, HttpServletResponse response) throws IOException;


    void showImageByProcessInstanceId(String processInstanceId, HttpServletResponse response) throws Exception;


    void showImageByDeploymentId(String deploymentId, HttpServletResponse response) throws Exception;


    void showImageByBpmnModel(BpmnModel bpmnModel, HttpServletResponse response) throws Exception;


    void showImageByModelId(String modelId, HttpServletResponse response) throws Exception;


    void processTracking(String processDefinitionId, String processInstanceId, OutputStream out) throws Exception;


    boolean isFinished(String processInstanceId);


    boolean isFinished2(String processInstanceId);


    String getLatestFormKeyByProcDefKey(String procDefKey);
}
