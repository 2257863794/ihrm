package com.ihrm.company;

import com.ihrm.company.dao.CompanyDao;
import com.ihrm.domain.company.Company;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)//单元测试环境
public class CompanyDaoTest {
    @Autowired
    private CompanyDao companyDao;

    @Test
    public void test(){
        //companyDao.save(company)；保存或更新（id）
        //companyDao.deleteById();根据id删除
        //findById(id)：根据id查询
        //companyDao.findAll() 查询全部
        Company company = companyDao.findById("1").get();
        System.out.println(company);
    }
    @Test
    public void test1(){
        //companyDao.save(company)；保存或更新（id）
        //companyDao.deleteById();根据id删除
        //findById(id)：根据id查询
        //companyDao.findAll() 查询全部
        List<Company> list = companyDao.findAll();
        System.out.println(list);
    }

}
