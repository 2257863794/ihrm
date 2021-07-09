package com.ihrm.test;

import com.ihrm.atte.service.ExcelImportService;
import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.domain.atte.vo.AtteUploadVo;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Test1 {
    @Test
    public void test02() throws FileNotFoundException {
        ExcelImportUtil<AtteUploadVo> util = new ExcelImportUtil<>(AtteUploadVo.class);
        FileInputStream stream = new FileInputStream(new File("E:\\【iHRM 人力资源管理系统】Java实战项目\\15-考勤模块开发\\demo.xlsx"));
        List<AtteUploadVo> list = util.readExcel(stream, 1, 0);//第一行，第一个单元格开始读取
        for (AtteUploadVo vo : list) {
            System.out.println(vo);
        }
    }
    @Test
    public void test01() throws ParseException {
        String latestEffectDate=(Calendar.getInstance().get(Calendar.YEAR)+1)+"-"+"20120201";
        String date="2021-06-03T16:00:00.000Z".substring(0,10);
        System.out.println(date);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date d = format.parse(date);

        System.out.println(d.toLocaleString());
    }
}
