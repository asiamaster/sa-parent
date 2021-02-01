package com.sa.util;

import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelUtils {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";


    public static Workbook getWorkbok(InputStream in, String filename) throws IOException {
        Workbook wb = null;
        if (filename.endsWith(EXCEL_XLS)) {
            wb = new HSSFWorkbook(in);
        } else if (filename.endsWith(EXCEL_XLSX)) {
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }


    public static void checkExcelVaild(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("文件不存在");
        }
        if (!(file.isFile() && (file.getName().endsWith(EXCEL_XLS) || file.getName().endsWith(EXCEL_XLSX)))) {
            throw new Exception("文件不是Excel");
        }
    }












    public static List<List<Map<String, Object>>> getSheetsDatas(InputStream is, int headerRow) {

        List<List<Map<String, Object>>> sheetsDatas = Lists.newArrayList();
        try {

            Workbook workbook = WorkbookFactory.create(is);
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                List<Map<String, Object>> sheetDatas = Lists.newArrayList();
                Sheet sheet = workbook.getSheetAt(sheetIndex);

                int rowIndex = 0;

                Map<Integer, String> rowIndexData = new HashMap<>();
                for (Row row : sheet) {

                    Map<String, Object> rowData = new HashMap<>();

                    if (rowIndex < headerRow) {
                        rowIndex++;
                        continue;
                    }
                    int lastCellNum = row.getLastCellNum();

                    if (rowIndex == headerRow || rowIndexData.isEmpty()) {
                        for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
                            Cell cell = row.getCell(colIndex);
                            if (cell == null) {
                                continue;
                            }
                            Object value = getValue(cell);
                            if (value == null) {
                                continue;
                            }

                            rowIndexData.put(colIndex, value.toString());
                        }
                        rowIndex++;
                        continue;
                    }

                    for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
                        rowData.put(rowIndexData.get(colIndex), getValue(row.getCell(colIndex)));
                    }
                    sheetDatas.add(rowData);
                    rowIndex++;
                }
                sheetsDatas.add(sheetDatas);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sheetsDatas;
    }


    private static Object getValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC: {
                Double value = cell.getNumericCellValue();
                if(isInteger(value)){
                    return value.longValue();
                }else{
                    return value;
                }
            }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return "错误";
            case BLANK:
                return "空";
            case FORMULA:
                return "错误";
            default:
                return cell.getStringCellValue();
        }
    }


    private static boolean isInteger(double d){
        return d % 1 ==0;
    }
}