package com.sa.activiti.component;

import org.activiti.bpmn.model.AssociationDirection;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.image.exception.ActivitiImageException;
import org.activiti.image.impl.DefaultProcessDiagramCanvas;
import org.activiti.image.util.ReflectUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class CustomProcessDiagramCanvas extends DefaultProcessDiagramCanvas {

    protected static Color LABEL_COLOR = new Color(0, 0, 0);
    private final Color LINE_LABEL_COLOR = new Color(30, 30, 120);
    protected static Color LINE_HIGH_LIGHTED_COLOR = new Color(80, 80, 200);
    protected static int PROCESS_PADDING = 0;
    protected static int FONT_SIZE = 12 ;

    private static volatile boolean flag = false;

    public CustomProcessDiagramCanvas(int width, int height, int minX, int minY, String imageType) {
        super(width, height, minX, minY, imageType);
    }

    public CustomProcessDiagramCanvas(int width, int height, int minX, int minY, String imageType,
                                      String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader) {
        super(width, height, minX, minY, imageType, activityFontName, labelFontName, annotationFontName,
                customClassLoader);
    }

    public void drawHighLight(boolean isStartOrEnd, int x, int y, int width, int height, Color color) {
        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();

        g.setPaint(color);

        g.setStroke(new BasicStroke(2.0F));
        if (isStartOrEnd) {
            g.drawOval(x, y, width, height);
        } else {
            RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 5, 5);
            g.draw(rect);
        }
        g.setPaint(originalPaint);
        g.setStroke(originalStroke);
    }

    public void drawSequenceflow(int[] xPoints, int[] yPoints, boolean conditional, boolean isDefault,
                                 boolean highLighted, double scaleFactor, Color color) {
        drawConnection(xPoints, yPoints, conditional, isDefault, "sequenceFlow", AssociationDirection.ONE, highLighted,
                scaleFactor, color);
    }

    public void drawConnection(int[] xPoints, int[] yPoints, boolean conditional, boolean isDefault,
                               String connectionType, AssociationDirection associationDirection, boolean highLighted, double scaleFactor,
                               Color color) {

        Paint originalPaint = g.getPaint();
        Stroke originalStroke = g.getStroke();

        g.setPaint(CONNECTION_COLOR);
        if ("association".equals(connectionType)) {
            g.setStroke(ASSOCIATION_STROKE);
        } else if (highLighted) {
            g.setPaint(color);

            g.setStroke(new BasicStroke(2.0F));
        }

        for (int i = 1; i < xPoints.length; i++) {
            Integer sourceX = xPoints[i - 1];
            Integer sourceY = yPoints[i - 1];
            Integer targetX = xPoints[i];
            Integer targetY = yPoints[i];
            Line2D.Double line = new Line2D.Double(sourceX, sourceY, targetX, targetY);
            g.draw(line);
        }

        if (isDefault) {
            Line2D.Double line = new Line2D.Double(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
            drawDefaultSequenceFlowIndicator(line, scaleFactor);
        }

        if (conditional) {
            Line2D.Double line = new Line2D.Double(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
            drawConditionalSequenceFlowIndicator(line, scaleFactor);
        }

        if (associationDirection.equals(AssociationDirection.ONE)
                || associationDirection.equals(AssociationDirection.BOTH)) {
            Line2D.Double line = new Line2D.Double(xPoints[xPoints.length - 2], yPoints[xPoints.length - 2],
                    xPoints[xPoints.length - 1], yPoints[xPoints.length - 1]);
            drawArrowHead(line, scaleFactor);
        }
        if (associationDirection.equals(AssociationDirection.BOTH)) {
            Line2D.Double line = new Line2D.Double(xPoints[1], yPoints[1], xPoints[0], yPoints[0]);
            drawArrowHead(line, scaleFactor);
        }
        g.setPaint(originalPaint);
        g.setStroke(originalStroke);
    }


    @Override
    public void drawLabel(String text, GraphicInfo graphicInfo) {
        this.drawLabel(text, graphicInfo, true);
    }


    @Override
    public void drawLabel(String text, GraphicInfo graphicInfo, boolean centered) {
        float interline = 1.0F;
        if (text != null && text.length() > 0) {
            Paint originalPaint = this.g.getPaint();
            Font originalFont = this.g.getFont();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 140);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            this.g.setPaint(LABEL_COLOR);
            this.g.setFont(LABEL_FONT);
            int wrapWidth = 100;
            int textY = (int) graphicInfo.getY();
            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FOREGROUND, this.g.getPaint());
            as.addAttribute(TextAttribute.FONT, this.g.getFont());
            AttributedCharacterIterator aci = as.getIterator();
            FontRenderContext frc = new FontRenderContext((AffineTransform) null, true, false);

            TextLayout tl;
            for (LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc); lbm.getPosition() < text.length(); textY = (int) ((float) textY + tl.getDescent() + tl.getLeading() + (interline - 1.0F) * tl.getAscent())) {
                tl = lbm.nextLayout((float) wrapWidth);
                textY = (int) ((float) textY + tl.getAscent());
                Rectangle2D bb = tl.getBounds();
                double tX = graphicInfo.getX();
                if (centered) {
                    tX += (double) ((int) (graphicInfo.getWidth() / 2.0D - bb.getWidth() / 2.0D));
                }
                tl.draw(this.g, (float) tX, (float) textY);
            }
            this.g.setFont(originalFont);
            this.g.setPaint(originalPaint);
        }
    }


    public void drawEventLabel(String text, GraphicInfo graphicInfo){
        this.drawEventLabel(text, graphicInfo, true);
    }

    public void drawEventLabel(String text, GraphicInfo graphicInfo, boolean centered) {
        float interline = 1.0F;
        if (text != null && text.length() > 0) {
            Paint originalPaint = this.g.getPaint();
            Font originalFont = this.g.getFont();

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setPaint(LINE_LABEL_COLOR);
            g.setFont(new Font(labelFontName, Font.PLAIN, FONT_SIZE-1));
            int wrapWidth = 100;
            int textY = (int) graphicInfo.getY();
            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FOREGROUND, this.g.getPaint());
            as.addAttribute(TextAttribute.FONT, this.g.getFont());
            AttributedCharacterIterator aci = as.getIterator();
            FontRenderContext frc = new FontRenderContext((AffineTransform) null, true, false);
            TextLayout tl;
            for (LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc); lbm.getPosition() < text.length(); textY = (int) ((float) textY + tl.getDescent() + tl.getLeading() + (interline - 1.0F) * tl.getAscent())) {
                tl = lbm.nextLayout((float) wrapWidth);
                textY = (int) ((float) textY + tl.getAscent());
                Rectangle2D bb = tl.getBounds();
                double tX = graphicInfo.getX();
                if (centered) {
                    tX += (double) ((int) (graphicInfo.getWidth() / 2.0D - bb.getWidth() / 2.0D));
                }
                tl.draw(this.g, (float) tX, (float) textY);
                tl.draw(this.g, (float) tX, (float) textY);
            }
            this.g.setFont(originalFont);
            this.g.setPaint(originalPaint);
        }
    }


    public void drawLabel(boolean highLighted, String text, GraphicInfo graphicInfo, boolean centered) {
        float interline = 1.0f;

        if (text != null && text.length() > 0) {
            Paint originalPaint = g.getPaint();
            Font originalFont = g.getFont();

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (highLighted) {
                g.setPaint(LINE_HIGH_LIGHTED_COLOR);
                g.setFont(new Font(labelFontName, Font.BOLD, FONT_SIZE-1));
            } else {
                g.setPaint(LINE_LABEL_COLOR);
                g.setFont(new Font(labelFontName, Font.PLAIN, FONT_SIZE-1));
            }

            int wrapWidth = 100;
            int textY = (int) graphicInfo.getY();


            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FOREGROUND, g.getPaint());
            as.addAttribute(TextAttribute.FONT, g.getFont());
            AttributedCharacterIterator aci = as.getIterator();
            FontRenderContext frc = new FontRenderContext(null, true, false);
            LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
            while (lbm.getPosition() < text.length()) {
                TextLayout tl = lbm.nextLayout(wrapWidth);
                textY += tl.getAscent();
                Rectangle2D bb = tl.getBounds();
                double tX = graphicInfo.getX();
                if (centered) {
                    tX += (int) (graphicInfo.getWidth() / 2 - bb.getWidth() / 2);
                }
                tl.draw(g, (float) tX, textY);
                tl.draw(g, (float) tX, textY);
                textY += tl.getDescent() + tl.getLeading() + (interline - 1.0f) * tl.getAscent();
            }

            g.setFont(originalFont);
            g.setPaint(originalPaint);
        }
    }

    @Override
    public BufferedImage generateBufferedImage(String imageType) {
        if (closed) {
            throw new ActivitiImageException("ProcessDiagramGenerator already closed");
        }


        minX = (minX <= PROCESS_PADDING) ? PROCESS_PADDING : minX;
        minY = (minY <= PROCESS_PADDING) ? PROCESS_PADDING : minY;
        BufferedImage imageToSerialize = processDiagram;
        if (minX >= 0 && minY >= 0) {
            imageToSerialize = processDiagram.getSubimage(
                    minX - PROCESS_PADDING,
                    minY - PROCESS_PADDING,
                    canvasWidth - minX + PROCESS_PADDING,
                    canvasHeight - minY + PROCESS_PADDING);
        }
        return imageToSerialize;
    }

    @Override
    public void initialize(String imageType) {
        this.processDiagram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        this.g = processDiagram.createGraphics();







        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(Color.black);

        Font font = new Font(activityFontName, Font.BOLD, FONT_SIZE);
        g.setFont(font);
        this.fontMetrics = g.getFontMetrics();

        LABEL_FONT = new Font(labelFontName, Font.ITALIC, FONT_SIZE);
        ANNOTATION_FONT = new Font(annotationFontName, Font.PLAIN, FONT_SIZE);

        if(flag) {
            return;
        }
        try {
            USERTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/userTask.png", customClassLoader));
            SCRIPTTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/scriptTask.png", customClassLoader));
            SERVICETASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/serviceTask.png", customClassLoader));
            RECEIVETASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/receiveTask.png", customClassLoader));
            SENDTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/sendTask.png", customClassLoader));
            MANUALTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/manualTask.png", customClassLoader));
            BUSINESS_RULE_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/businessRuleTask.png", customClassLoader));
            SHELL_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/shellTask.png", customClassLoader));
            CAMEL_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/camelTask.png", customClassLoader));
            MULE_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/muleTask.png", customClassLoader));

            TIMER_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/timer.png", customClassLoader));
            COMPENSATE_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/compensate-throw.png", customClassLoader));
            COMPENSATE_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/compensate.png", customClassLoader));
            ERROR_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/error-throw.png", customClassLoader));
            ERROR_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/error.png", customClassLoader));
            MESSAGE_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/message-throw.png", customClassLoader));
            MESSAGE_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/message.png", customClassLoader));
            SIGNAL_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/signal-throw.png", customClassLoader));
            SIGNAL_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/activiti/icons/signal.png", customClassLoader));

            flag = true;
        } catch (IOException e) {
            flag = false;
            LOGGER.warn("Could not load image for process diagram creation: {}", e.getMessage());
        }
    }


}