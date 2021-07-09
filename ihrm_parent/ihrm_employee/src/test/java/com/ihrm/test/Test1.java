package com.ihrm.test;

import com.ihrm.employee.dao.TransferPositionDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

public class Test1 {
    @Autowired
    private TransferPositionDao transferPositionDao;
    public void test1(){
        Map map = transferPositionDao.findPostAndTimeCause("");
        System.out.println(map);
    }
}
