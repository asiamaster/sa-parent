package com.sa.activiti.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sa.activiti.component.CustomBpmnJsonConverter;
import com.sa.activiti.consts.ActivitiConstants;
import com.sa.activiti.dto.ActModelVO;
import com.sa.activiti.service.ActivitiService;
import com.sa.activiti.util.ImageGenerator;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


@Service
@ConditionalOnExpression("'${activiti.enable}'=='true'")
public class ActivitiServiceImpl implements ActivitiService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private FormService formService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SpringProcessEngineConfiguration springProcessEngineConfiguration;

    private final Logger log = LoggerFactory.getLogger(ActivitiServiceImpl.class);

    private final Color LINE_COLOR = new Color(30, 160, 30);

    private final Color ACTIVITY_COLOR = new Color(200, 30, 30);


    @Override
    @Transactional
    public Model createModel(String key, String name, String description, String category) throws UnsupportedEncodingException {
        ActModelVO actModelVO = new ActModelVO();
        actModelVO.setKey(key);
        actModelVO.setName(name);
        actModelVO.setDescription(description);
        actModelVO.setCategory(category);
        return createModel(actModelVO);
    }

    @Override
    @Transactional
    public Model createModel(ActModelVO actModelVO) throws UnsupportedEncodingException {

        Model model = repositoryService.newModel();
        model.setKey(StringUtils.defaultString(actModelVO.getKey()));
        model.setName(actModelVO.getName());
        model.setCategory(actModelVO.getCategory());
        model.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(model.getKey()).count()+1)));
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, model.getName());
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, actModelVO.getDescription());
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);

        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace","http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(model.getId(),editorNode.toString().getBytes("utf-8"));
        return model;
    }


    @Override
    public void createModel(BpmnModel bpmnModel, String filename, String deploymentId) throws IOException, XMLStreamException {
        Model model = repositoryService.newModel();
        model.setDeploymentId(deploymentId);
        model.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(bpmnModel.getMainProcess().getId()).count()+1)));
        model.setName(filename);
        model.setKey(bpmnModel.getMainProcess().getId());
        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, filename);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, bpmnModel.getMainProcess().getDocumentation());
        model.setMetaInfo(modelObjectNode.toString());
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);

        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), objectNode.toString().getBytes("utf-8"));
    }


    @Override
    public void saveModel(String modelId, String name, String description, String json_xml, String svg_xml){
        try {
            Model model = repositoryService.getModel(modelId);
            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);

            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));













            ObjectNode modelNode = (ObjectNode) new ObjectMapper()
                    .readTree(json_xml.getBytes("utf-8"));
            BpmnModel bpmnModel = new CustomBpmnJsonConverter().convertToBpmnModel(modelNode);


            final byte[] result = ImageGenerator.generateDiagram(bpmnModel, ActivitiConstants.FONT_NAME, ActivitiConstants.FONT_NAME, ActivitiConstants.FONT_NAME, springProcessEngineConfiguration.getClassLoader());

            repositoryService.addModelEditorSourceExtra(model.getId(), result);
        } catch (Exception e) {
            log.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }


    @Override
    public Deployment deployByModelId(String modelId) throws ActivitiException, IOException {

        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            throw new ActivitiException("模型数据为空，请先设计流程并成功保存，再进行发布");
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel bpmnModel = new CustomBpmnJsonConverter().convertToBpmnModel(modelNode);
        if(bpmnModel.getProcesses().size() == 0){
            throw new ActivitiException("数据模型不符要求，请至少设计一条主线流程");
        }

        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = null;
        try {
            deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .enableDuplicateFiltering()


                    .addBpmnModel(processName, bpmnModel)
                    .deploy();
        } catch (org.activiti.bpmn.exceptions.XMLException e) {
            e.printStackTrace();
            throw new ActivitiException(e.getMessage());
        }
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return deployment;
    }


    @Override
    public Deployment deployFormByModelId(String modelId) throws ActivitiException, IOException {

        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            throw new ActivitiException("模型数据为空，请先设计流程并成功保存，再进行发布");
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel bpmnModel = new CustomBpmnJsonConverter().convertToBpmnModel(modelNode);
        if(bpmnModel.getProcesses().size() == 0){
            throw new ActivitiException("数据模型不符要求，请至少设计一条主线流程");
        }

        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = null;
        try {
            deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .enableDuplicateFiltering()
                    .addBpmnModel(processName, bpmnModel)
                    .addString("start.form", "<input type=\"text\" id=\"name\" name=\"name\"></input>")
                    .deploy();
        } catch (org.activiti.bpmn.exceptions.XMLException e) {
            e.printStackTrace();
            throw new ActivitiException("流程节点的id不能以数字开头");
        }
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return deployment;
    }


    @Override
    public Model getModelById(String modelId){
        return repositoryService.getModel(modelId);
    }


    @Override
    @Transactional
    public void deleteModel(String modelId){
        repositoryService.deleteModel(modelId);
    }


    @Override
    public Page<Model> getAllModels(Pageable pageable){
        ModelQuery modelQuery = repositoryService.createModelQuery().latestVersion().orderByLastUpdateTime().desc();
        return new PageImpl<>(modelQuery.listPage(pageable.getPageNumber()*pageable.getPageSize(), pageable.getPageSize()), pageable, modelQuery.count());
    }


    @Override
    @Transactional
    public void deleteDeployment(String deploymentId){
        repositoryService.deleteDeployment(deploymentId);
    }


    @Override
    public void exportModel(String modelId, HttpServletResponse response) throws IOException{
        Model modelData = repositoryService.getModel(modelId);
        BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
        ObjectNode editorNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
        BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
        ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
        IOUtils.copy(in, response.getOutputStream());
        String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
        response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(filename, "UTF-8"));
        response.flushBuffer();
    }


    @Override
    public void showImageByProcessInstanceId(String processInstanceId, HttpServletResponse response) throws Exception{
        showImageByBpmnModel(repositoryService.getBpmnModel(processInstanceId), response);
    }


    @Override
    public void showImageByDeploymentId(String deploymentId, HttpServletResponse response) throws Exception{










        List<String> list = repositoryService.getDeploymentResourceNames(deploymentId);

        String resourceName = "";
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.indexOf(".png") >= 0 || name.indexOf(".jpg") >= 0) {
                    resourceName = name;
                    break;
                }
            }
        }
        InputStream inputStream = repositoryService.getResourceAsStream(deploymentId,resourceName);
        int b = -1;
        OutputStream outputStream = response.getOutputStream();
        while ((b=inputStream.read())!=-1){
            outputStream.write(b);
        }
        inputStream.close();
        outputStream.close();
    }


    @Override
    public void showImageByBpmnModel(BpmnModel bpmnModel, HttpServletResponse response) throws Exception{
        InputStream is = ImageGenerator.generateDiagram(bpmnModel, "png",
                null, null, ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,
                springProcessEngineConfiguration.getClassLoader(),1.0, new Color[]{LINE_COLOR, ACTIVITY_COLOR});
        OutputStream outputStream = response.getOutputStream();
        int b = -1 ;
        while ((b=is.read())!=-1){
            outputStream.write(b);
        }
        is.close();
        outputStream.close();
    }


    @Override
    public void showImageByModelId(String modelId, HttpServletResponse response) throws Exception{
        Model modelData = repositoryService.getModel(modelId);
        ObjectNode modelNode = (ObjectNode) new ObjectMapper()
                .readTree(repositoryService.getModelEditorSource(modelData.getId()));
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        showImageByBpmnModel(bpmnModel, response);
    }


    @Override
    public void processTracking(String processDefinitionId, String processInstanceId, OutputStream out) throws Exception {
        if(StringUtils.isBlank(processDefinitionId)){
            processDefinitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
        }

        List<String> activeActivityIds = new ArrayList<String>();
        List<String> highLightedFlows = new ArrayList<String>();


        if (this.isFinished(processInstanceId)) {

            List<HistoricActivityInstance> historicActivityInstances = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId).activityType("endEvent")
                    .list();
            for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
                activeActivityIds.add(historicActivityInstance.getActivityId());
            }
        } else {

            activeActivityIds = runtimeService
                    .getActiveActivityIds(processInstanceId);
        }



        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();

        highLightedFlows = this.getHighLightedFlows((ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                .getDeployedProcessDefinition(processDefinitionId), historicActivityInstances);


            InputStream imageStream = null;
            try {

                BpmnModel bpmnModel = repositoryService
                        .getBpmnModel(processDefinitionId);






                imageStream = ImageGenerator.generateDiagram(bpmnModel, "png",
                        activeActivityIds, highLightedFlows, ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,

                        springProcessEngineConfiguration.getClassLoader(),1.0, new Color[]{LINE_COLOR, ACTIVITY_COLOR});
                IOUtils.copy(imageStream, out);
            } finally {
                IOUtils.closeQuietly(imageStream);
            }

    }


    @Override
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count() > 0;
    }


    @Override
    public boolean isFinished2(String processInstanceId) {
        ProcessInstance pi = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        return pi == null ? true : false;
    }

    @Override
    public String getLatestFormKeyByProcDefKey(String procDefKey) {

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).latestVersion().singleResult();
        return formService.getStartFormKey(processDefinition.getId());
    }


    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<>();
        List<String> highActivitiImpl = new ArrayList<>();
        for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
            highActivitiImpl.add(historicActivityInstance.getActivityId());
        }
        for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
            ActivityImpl activityImpl = processDefinitionEntity.findActivity(historicActivityInstance.getActivityId());
            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();

            for (PvmTransition pvmTransition : pvmTransitions) {

                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
                if (highActivitiImpl.contains(pvmActivityImpl.getId())) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }












































































}
