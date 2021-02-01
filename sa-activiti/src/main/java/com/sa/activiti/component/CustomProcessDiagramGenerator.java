package com.sa.activiti.component;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.image.ProcessDiagramGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

public interface CustomProcessDiagramGenerator extends ProcessDiagramGenerator {

    InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities, List<String> highLightedFlows, String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader, double scaleFactor, Color[] colors);

    @Override
    BufferedImage generatePngImage(BpmnModel bpmnModel, double scaleFactor);

    BufferedImage generatePngImage(BpmnModel bpmnModel);

    @Override
    InputStream generateDiagram(BpmnModel bpmnModel, String imageType, String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader);
}
