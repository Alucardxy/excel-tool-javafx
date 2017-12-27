package com.alucardxy.exceltool.util;

import org.apache.poi.ss.usermodel.Cell;

/**
 * @author Alucardxy
 * @date 16/5/16 18:37
 */
public class CellUtil {
    public static String getValueFromCell(Cell cell){
        String value = null;
        if(cell!=null){
            switch (cell.getCellTypeEnum()){
                case STRING:value = cell.getStringCellValue();break;
                case NUMERIC:case FORMULA:{
                    double num = cell.getNumericCellValue();
                    if(num%1==0) value = String.valueOf((int)num);
                    else value = String.valueOf(num);
                }break;
                case BOOLEAN:value = String.valueOf(cell.getBooleanCellValue());break;
                default:value = "";break;
            }
        }
        return value;
    }
}
