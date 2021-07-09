package com.ihrm.common.poi;

import com.ihrm.domain.poi.ExcelAttribute;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ExcelImportUtil<T> {

    private Class clazz;
    private  Field fields[];//保存属性

    //参数：
    //Class类的对象作用是运行时提供或获得某个对象的类型信息
    public ExcelImportUtil(Class clazz) {
        this.clazz = clazz;//
        //获取所有的公有的和私有的属性
        fields = clazz.getDeclaredFields();
    }

    public ExcelImportUtil() {

    }

    /**
     * 基于注解读取excel
     *      is     : 文件上传的流信息
     *      rowIndex:   读取数据的起始行
     *      cellIndex:  读取数据的其实单元格位置
     *
     */
    public List<T> readExcel(InputStream is, int rowIndex,int cellIndex) {
        List<T> list = new ArrayList<T>();
        T entity = null;
        try {
            //创建工作簿，XSSFWorkbook对象：可以导入百万数据
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowNum = rowIndex; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                entity = (T) clazz.newInstance();
                for (int j = cellIndex; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    for (Field field : fields) {
                        //ExcelAttribute作用：就是将我们的实体类的属性与我们的excel表里面的列对应起来
                        if(field.isAnnotationPresent(ExcelAttribute.class)){
                            //将一个类的成员变量置为private如果字段是私有的,那么必须要对这个字段设置
                            field.setAccessible(true);
                            //通过这个属性，得到这个注解
                            ExcelAttribute ea = field.getAnnotation(ExcelAttribute.class);
                            //通过这个注解得到具体的注解中定义的属性的值
                            if(j == ea.sort()) {
                                System.out.println(covertAttrType(field, cell));
                                field.set(entity, covertAttrType(field, cell));
                            }
                        }
                    }
                }
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 类型转换 将cell 单元格格式转为 字段类型
     */
    private Object covertAttrType(Field field, Cell cell) throws Exception {
        String fieldType = field.getType().getSimpleName();
        if ("String".equals(fieldType)) {
            return getValue(cell);
        }else if ("Date".equals(fieldType)) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getValue(cell)) ;
        }else if ("int".equals(fieldType) || "Integer".equals(fieldType)) {
            return Integer.parseInt(getValue(cell));
        }else if ("double".equals(fieldType) || "Double".equals(fieldType)) {
            return Double.parseDouble(getValue(cell));
        }else {
            return null;
        }
    }


    /**
     * 格式转为String
     * @param cell
     * @return
     */
    public String getValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date dt = DateUtil.getJavaDate(cell.getNumericCellValue());
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt);
                } else {
                    // 防止数值变成科学计数法
                    String strCell = "";
                    Double num = cell.getNumericCellValue();
                    BigDecimal bd = new BigDecimal(num.toString());
                    if (bd != null) {
                        strCell = bd.toPlainString();
                    }
                    // 去除 浮点型 自动加的 .0
                    if (strCell.endsWith(".0")) {
                        strCell = strCell.substring(0, strCell.indexOf("."));
                    }
                    return strCell;
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}