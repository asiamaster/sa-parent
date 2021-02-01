package com.sa.activiti.controller;

import com.sa.activiti.service.ActivitiService;
import com.sa.activiti.util.ActivitiUtils;
import com.sa.domain.BaseOutput;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;


@Controller
@RequestMapping("/modeler")
public class ModelerController implements ModelDataJsonConstants {

    private final Logger log = LoggerFactory.getLogger(ModelerController.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ActivitiService activitiService;


    @RequestMapping(value = "/create.html", method = {RequestMethod.POST, RequestMethod.GET})
    public String createModel(@RequestParam("name") String name,
                              @RequestParam(value="key", required=false) String key,
                              @RequestParam(value="description", required=false) String description,
                              @RequestParam(value="category", required=false) String category,
                              HttpServletRequest request, RedirectAttributes attr) throws IOException {
        Model model = activitiService.createModel(key, name, description, category);
        attr.addAttribute("modelId", model.getId());
        attr.addAttribute("closeUrl", request.getParameter("closeUrl"));
        return "redirect:"+request.getContextPath()+"/modeler.html";

    }

    @RequestMapping(value = "/{modelId}/save.action", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, String name, String description, String json_xml, String svg_xml) {
       activitiService.saveModel(modelId, name, description, json_xml, svg_xml);
    }


    @ResponseBody
    @RequestMapping("/uploadBpmnXml.action")
    public BaseOutput uploadBpmnXml(MultipartHttpServletRequest request) throws IOException, XMLStreamException {
        for (MultipartFile file : request.getFileMap().values()) {
            String filename = file.getOriginalFilename();
            if (filename.indexOf(".bpmn20") > 0) {
                filename = filename.substring(0, filename.indexOf(".bpmn20"));
            } else {
                filename = filename.substring(0, filename.indexOf(".xml"));
            }
            BpmnModel bpmnModel = ActivitiUtils.toBpmnModel(file.getInputStream());
            String errorMsg = ActivitiUtils.validateBpmnModel(bpmnModel);
            if(errorMsg != null){
                return BaseOutput.failure(errorMsg);
            }

            Deployment deployment = repositoryService.createDeployment().name(filename).addInputStream(filename + ".bpmn20.xml", file.getInputStream()).deploy();

            activitiService.createModel(bpmnModel, filename, deployment.getId());















        }
        return BaseOutput.success();
    }


    @RequestMapping(value = "/export.action", method = {RequestMethod.GET})
    public void exportModel(@RequestParam String modelId, HttpServletResponse response) throws IOException{
        activitiService.exportModel(modelId, response);
    }


    @RequestMapping(value = "/img.action", method = {RequestMethod.GET})
    public void showImageByModelId(@RequestParam String modelId, HttpServletResponse response) throws Exception{
        activitiService.showImageByModelId(modelId, response);
    }

}
