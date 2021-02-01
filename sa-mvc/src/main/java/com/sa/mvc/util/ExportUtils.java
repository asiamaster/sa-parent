package com.sa.mvc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sa.component.CustomThreadPoolExecutorCache;
import com.sa.domain.ExportParam;
import com.sa.domain.TableHeader;
import com.sa.exception.AppException;
import com.sa.metadata.ValueProvider;
import com.sa.util.BeanConver;
import com.sa.util.OkHttpUtils;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;


@Component
@DependsOn("initConfig")
public class ExportUtils {

    public final static Logger log = LoggerFactory.getLogger(ExportUtils.class);


    private final static int FETCH_COUNT = 20000;

    private static final String HEADER_HIDDEN = "hidden";
    private static final String HEADER_PROVIDER = "provider";
    private static final String HEADER_FIELD = "field";
    private static final String HEADER_TYPE = "type";
    private static final String HEADER_FORMAT = "format";
    private static final String HEADER_EXPORT = "export";
    private static final String HEADER_TITLE = "title";


    private final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";


    private final static String CONTENT_TYPE_JSON = "application/json";


    @Resource
    private CustomThreadPoolExecutorCache customThreadPoolExecutor;


    public void exportBeans(HttpServletResponse response, String title, List<TableHeader> tableHeaders, List beans, Map providerMeta) throws Exception {
        List<Map> datas = new ArrayList<>(beans.size());
        for (Object bean : beans) {
            Map map = BeanConver.transformObjectToMap(bean);
            datas.add(map);
        }
        exportMaps(response, title, tableHeaders, datas, providerMeta);
    }


    public void exportMaps(HttpServletResponse response, String title, List<TableHeader> tableHeaders, List<Map> datas, Map providerMeta) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(FETCH_COUNT);
        Sheet sheet = workbook.createSheet(title);

        CellStyle columnTopStyle = getHeaderColumnStyle(workbook);
        Row headerRow = sheet.createRow(0);
        for (int j = 0; j < tableHeaders.size(); j++) {

            TableHeader tableHeader = tableHeaders.get(j);
            Cell cell = headerRow.createCell(j, CellType.STRING);
            RichTextString text = new XSSFRichTextString(tableHeader.getTitle().replaceAll("\\n", "").trim());
            cell.setCellValue(text);
            cell.setCellStyle(columnTopStyle);
        }

        Map<String, CellStyle> DATA_COLUMN_STYLE = new HashMap<String, CellStyle>();


        Map<String, ValueProvider> providerBuffer = new HashMap<>();

        for (int i = 0; i < datas.size(); i++) {
            Map rowDataMap = datas.get(i);
            Row dataRow = sheet.createRow(i + 1);

            for (int j = 0; j < tableHeaders.size(); j++) {
                TableHeader tableHeader = tableHeaders.get(j);
                Object value = rowDataMap.get(tableHeader.getField());
                String format = tableHeader.getFormat() == null ? getDefaultFormat(value) : tableHeader.getFormat();

                CellStyle dataColumnStyle = getDataColumnStyle(workbook, format, DATA_COLUMN_STYLE);
                if (providerMeta != null && providerMeta.containsKey(tableHeader.getField())) {
                    ValueProvider valueProvider = null;

                    String providerBeanId = (String) providerMeta.get(tableHeader.getField());
                    if (providerBuffer.containsKey(providerBeanId)) {
                        valueProvider = providerBuffer.get(providerBeanId);
                    } else {
                        valueProvider = SpringUtil.getBean(providerBeanId, ValueProvider.class);
                        providerBuffer.put(providerBeanId, valueProvider);
                    }
                    setCellValue(dataRow, j, valueProvider.getDisplayText(value, null, null), dataColumnStyle, tableHeader.getTitle());
                } else {
                    setCellValue(dataRow, j, value, dataColumnStyle, tableHeader.getTitle());
                }
            }
        }

        write(title, workbook, response);
    }


    public void export(HttpServletRequest request, HttpServletResponse response, ExportParam exportParam) {
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook(FETCH_COUNT);
            String title = exportParam.getTitle();
            SXSSFSheet sheet = workbook.createSheet(StringUtils.isBlank(title) ? "sheet1" : title);

            sheet.trackAllColumnsForAutoSizing();

            buildHeader(exportParam, workbook, sheet);

            buildData(exportParam, workbook, sheet, request);

            write(exportParam.getTitle(), workbook, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void buildData(ExportParam exportParam, SXSSFWorkbook workbook, Sheet sheet, HttpServletRequest request) {
        String url = exportParam.getUrl();
        if (!url.startsWith("http")) {
            url = url.startsWith("/") ? url : "/" + url;
            String basePath = SpringUtil.getProperty("project.serverPath", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
            url = basePath + url;
        }




        int total = 0;
        try {
            total = getCount(url, exportParam.getContentType(), exportParam.getQueryParams(), request);
        } catch (Exception e) {
            log.error(String.format("构建导出数据异常, url:'%s', 参数:(%s)", url, JSON.toJSONString(exportParam.getQueryParams())), e);
            throw e;
        }

        int queryCount = total % FETCH_COUNT == 0 ? total / FETCH_COUNT : total / FETCH_COUNT + 1;

        if (queryCount == 1) {
            JSONArray rowDatas = new ExportDataThread(0, exportParam.getQueryParams(), url, exportParam.getContentType(), request).queryThreadData();
            buildSingleData(workbook, 0, exportParam.getColumns(), rowDatas, sheet);
        } else {

            List<Future<JSONArray>> futures = new ArrayList<>(queryCount);
            for (int current = 0; current < queryCount; current++) {
                Map<String, String> queryParams = new HashMap<>();


                queryParams.putAll(exportParam.getQueryParams());
                CompletionService<JSONArray> completionService = new ExecutorCompletionService<JSONArray>(customThreadPoolExecutor.getExecutor());
                Future<JSONArray> future = completionService.submit(new ExportDataThread(current, queryParams, url, exportParam.getContentType(), request));

                futures.add(future);
            }
            int current = 0;
            try {
                for (Future<JSONArray> future : futures) {
                    JSONArray rowDatas = future.get();
                    if(rowDatas == null){
                        continue;
                    }
                    buildSingleData(workbook, current++, exportParam.getColumns(), rowDatas, sheet);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private String getDefaultFormat(Object value){
        if (value instanceof Integer || value instanceof Short || value instanceof Long) {
            return "0";
        } else if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return "0.00";
        } else if (value instanceof Date){
            return "m/d/yy h:mm";
        } else{
            return "@";
        }
    }


    private void buildSingleData(SXSSFWorkbook workbook, int current, List<List<Map<String, Object>>> columns, JSONArray rowDatas, Sheet sheet) {
        Integer headerRowCount = columns.size();

        List<Map<String, Object>> headers = columns.get(columns.size() - 1);
        int headerSize = headers.size();
        int rowDataSize = rowDatas.size();

        Map<String, CellStyle> DATA_COLUMN_STYLE = new HashMap<String, CellStyle>();

        for (int i = 0; i < rowDataSize; i++) {
            JSONObject rowDataMap = (JSONObject) rowDatas.get(i);
            Row row = sheet.createRow(current * FETCH_COUNT + i + headerRowCount);
            int cellIndex = 0;

            for (int j = 0; j < headerSize; j++) {
                Map<String, Object> headerMap = headers.get(j);
                Boolean export = (Boolean)headerMap.get(HEADER_EXPORT);
                if(export != null && !export){
                    continue;
                }

                if (headerMap.get(HEADER_HIDDEN) != null && headerMap.get(HEADER_HIDDEN).equals(true) && (export == null || !export)) {
                    continue;
                }
                String field = (String) headerMap.get(HEADER_FIELD);
                Object value = null;
                int fieldIndex = field.indexOf(".");

                if (StringUtils.isBlank((String) headerMap.get(HEADER_PROVIDER)) && fieldIndex >= 0) {
                    String field1 = field.substring(0, fieldIndex);
                    String field2 = field.substring(fieldIndex + 1);
                    JSONObject obj = rowDataMap.getJSONObject(field1);
                    if (obj != null) {
                        value = obj.get(field2);
                    }
                } else {
                    value = rowDataMap.get(field);
                }

                String type = (String) headerMap.get(HEADER_TYPE);

                String format = headerMap.getOrDefault(HEADER_FORMAT, getDefaultFormat(value)).toString();




                CellStyle dataColumnStyle = getDataColumnStyle(workbook, format, DATA_COLUMN_STYLE);
                setCellValue(row, cellIndex, value, dataColumnStyle, type);
                cellIndex++;
            }
        }
    }


    private void setCellValue(Row row, int cellIndex,  Object value, CellStyle dataColumnStyle, String type) {
        CellType cellType = value instanceof Number ? CellType.NUMERIC : CellType.STRING;
        if(StringUtils.isNotBlank(type) && type.equals("number")){
            cellType = CellType.NUMERIC;
        }
        Cell cell = row.createCell(cellIndex, cellType);
        cell.setCellStyle(dataColumnStyle);
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if(StringUtils.isNotBlank(type)){
            if(type.equals("number")){
                cell.setCellValue(new Double(value.toString()));
            }else if(type.equals("string")){
                cell.setCellValue(new XSSFRichTextString(value.toString()));
            }else{
                cell.setCellValue(value.toString());
            }
            return;
        }

        if (value instanceof Integer) {
            cell.setCellValue(((Integer) value).doubleValue());
        } else if (value instanceof Long) {
            cell.setCellValue(((Long) value).doubleValue());
        } else if (value instanceof Short) {
            cell.setCellValue(((Short) value).doubleValue());
        } else if (value instanceof Float) {
            cell.setCellValue(((Float) value).doubleValue());
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else {
            RichTextString text = new XSSFRichTextString(value.toString());
            cell.setCellValue(text);
        }
    }


    private class ExportDataThread implements Callable {
        int current;
        Map<String, String> queryParams;
        String exportUrl;
        String contentType;
        HttpServletRequest request;

        ExportDataThread(int current, Map<String, String> queryParams, String exportUrl, String contentType, HttpServletRequest request) {
            this.current = current;
            this.queryParams = queryParams;
            this.exportUrl = exportUrl;
            this.request = request;
            this.contentType = contentType;
        }

        @Override
        public JSONArray call() {
            return queryThreadData();
        }


        private JSONArray queryThreadData() {
            queryParams.put("page", String.valueOf(current + 1));
            queryParams.put("rows", String.valueOf(FETCH_COUNT));
            String json = syncExecute(exportUrl, contentType, queryParams, "POST", request);

            if (json.trim().startsWith("[") && json.trim().endsWith("]")) {
                return JSON.parseArray(json);
            } else {
                return (JSONArray) JSON.parseObject(json).get("rows");
            }
        }
    }


    private int getCount(String url, String contentType, Map<String, String> queryParams, HttpServletRequest request) {
        queryParams.put("page", "1");
        queryParams.put("rows", "1");
        String json = syncExecute(url, contentType, queryParams, "POST", request);
        if(json == null){
            String exMsg = String.format("远程访问异常, url:%s, contentType:%s, queryParams:%s", url, contentType, queryParams);
            log.error(exMsg);
            throw new AppException(exMsg);
        }

        if (json.trim().startsWith("[") && json.trim().endsWith("]")) {
            return JSON.parseArray(json).size();
        } else {
            try {
                return (int) JSON.parseObject(json).get("total");
            } catch (Exception e) {
                log.error("getCount远程访问失败，结果:" + json);
                throw e;
            }
        }
    }


    private void buildHeader(ExportParam exportParam, SXSSFWorkbook workbook, Sheet sheet) {
        CellStyle columnTopStyle = getHeaderColumnStyle(workbook);

        for (int i = 0; i < exportParam.getColumns().size(); i++) {

            List<Map<String, Object>> rowColumns = exportParam.getColumns().get(i);
            Row row = sheet.createRow(i);
            int colspanAdd = 0;
            int index = 0;

            Iterator<Map<String, Object>> it = rowColumns.iterator();

            int columnIndex = 0;
            while (it.hasNext()) {



                Map<String, Object> columnMap = it.next();
                Boolean export = (Boolean)columnMap.get(HEADER_EXPORT);
                if(export != null && !export){
                    continue;
                }

                if (columnMap.get(HEADER_HIDDEN) != null && columnMap.get(HEADER_HIDDEN).equals(true) && (export == null || !export)) {
                    it.remove();
                    continue;
                }
                if (columnMap.get(HEADER_TITLE) == null) {
                    it.remove();
                    continue;
                }
                String headerTitle = columnMap.get(HEADER_TITLE).toString().replaceAll("\\n", "").trim();

                if (i == exportParam.getColumns().size() - 1) {
                    sheet.setColumnWidth(columnIndex, headerTitle.getBytes().length * 2 * 256);

                }
                Cell cell = row.createCell(index + colspanAdd, CellType.STRING);
                RichTextString text = new XSSFRichTextString(headerTitle);
                cell.setCellValue(text);
                cell.setCellStyle(columnTopStyle);
                if (columnMap.get("colspan") != null) {
                    Integer colspan = Integer.class.isAssignableFrom(columnMap.get("colspan").getClass()) ? (Integer) columnMap.get("colspan") : Integer.parseInt(columnMap.get("colspan").toString());
                    if (colspan > 1) {
                        Cell tempCell = row.createCell(index + colspanAdd + colspan - 1, CellType.STRING);
                        tempCell.setCellStyle(columnTopStyle);
                        sheet.addMergedRegion(new CellRangeAddress(i, i, index + colspanAdd, index + colspanAdd + colspan - 1));
                        colspanAdd = colspanAdd + colspan - 1;
                    }
                }
                columnIndex++;
                index++;
            }
        }
    }


    private String syncExecute(String url, String contentType, Map<String, String> queryParams, String httpMethod, HttpServletRequest request) {
        try {
            Map<String, String> headersMap = new HashMap<>();


            if (request != null) {
                Enumeration<String> enumeration = request.getHeaderNames();
                while (enumeration.hasMoreElements()) {
                    String key = enumeration.nextElement();
                    if ("Accept-Encoding".equalsIgnoreCase(key.trim())) {
                        continue;

                    } else if ("Host".equalsIgnoreCase(key.trim())) {
                        continue;
                    }
                    headersMap.put(key, request.getHeader(key));
                }
            }
            if ("POST".equalsIgnoreCase(httpMethod)) {
                JSONObject paramJo = (JSONObject) JSONObject.toJSON(queryParams);
                if (StringUtils.isBlank(contentType) || contentType.indexOf(CONTENT_TYPE_FORM) >= 0) {

                    Map<String, String> param = buildMetadataParams(paramJo);

                    return OkHttpUtils.postFormParameters(url, param, headersMap, null);
                } else if (contentType.indexOf(CONTENT_TYPE_JSON) >= 0) {
                    return OkHttpUtils.postBodyString(url, paramJo.toJSONString(), headersMap, null);
                } else {
                    log.error(String.format("不支持的contentType[%s]", contentType));
                    return null;
                }
            } else {
                return OkHttpUtils.get(url, queryParams, null, null);
            }
        } catch (Exception e) {
            log.error(String.format("远程调用[" + url + "]发生异常,message:[%s]", e.getMessage()), e);
            return null;
        }
    }


    private Map<String, String> buildMetadataParams(JSONObject paramJo){
        Map<String, String> param = new HashMap<>();
        if (paramJo != null && !paramJo.isEmpty()) {
            for (Map.Entry<String, Object> entry : paramJo.entrySet()) {
                if (entry.getValue() instanceof JSONObject) {
                    JSONObject valueJo = (JSONObject) entry.getValue();

                    for (Map.Entry<String, Object> tmpEntry : valueJo.entrySet()) {
                        if (tmpEntry.getValue() == null) {
                            continue;
                        }
                        param.put(entry.getKey() + "[" + tmpEntry.getKey() + "]", tmpEntry.getValue().toString());
                    }
                } else {

                    if (entry.getValue() == null) {
                        continue;
                    }
                    param.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        return param;
    }


    private void write(String title, SXSSFWorkbook workbook, HttpServletResponse response) {
        if (workbook != null) {
            try {
                String fileNameDownload = title + ".xlsx";

                response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileNameDownload, "utf-8") + "\"");

                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                OutputStream out = response.getOutputStream();
                workbook.write(out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private CellStyle getHeaderColumnStyle(SXSSFWorkbook workbook) {

        Font font = workbook.createFont();

        font.setFontHeightInPoints((short) 11);

        font.setBold(true);

        font.setColor(IndexedColors.ROYAL_BLUE.index);

        font.setFontName("Courier New");

        CellStyle style = workbook.createCellStyle();

        style.setBorderBottom(BorderStyle.THIN);

        style.setBottomBorderColor(IndexedColors.BLACK.index);

        style.setBorderLeft(BorderStyle.THIN);

        style.setLeftBorderColor(IndexedColors.BLACK.index);

        style.setBorderRight(BorderStyle.THIN);

        style.setRightBorderColor(IndexedColors.BLACK.index);

        style.setBorderTop(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.BLACK.index);

        style.setFont(font);

        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);

        style.setWrapText(false);

        style.setAlignment(HorizontalAlignment.CENTER);

        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }


    private CellStyle getDataColumnStyle(SXSSFWorkbook workbook, String format, Map<String, CellStyle> DATA_COLUMN_STYLE) {
        if(DATA_COLUMN_STYLE.containsKey(format)){
            return DATA_COLUMN_STYLE.get(format);
        }

        Font font = workbook.createFont();

        font.setFontHeightInPoints((short) 10);



        font.setFontName("Courier New");

        CellStyle style = workbook.createCellStyle();

        style.setBorderBottom(BorderStyle.THIN);

        style.setBottomBorderColor(IndexedColors.BLACK.index);

        style.setBorderLeft(BorderStyle.THIN);

        style.setLeftBorderColor(IndexedColors.BLACK.index);

        style.setBorderRight(BorderStyle.THIN);

        style.setRightBorderColor(IndexedColors.BLACK.index);

        style.setBorderTop(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.BLACK.index);

        style.setFont(font);

        style.setWrapText(false);

        style.setAlignment(HorizontalAlignment.CENTER);

        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat(format));
        DATA_COLUMN_STYLE.put(format, style);
        return style;
    }

}
