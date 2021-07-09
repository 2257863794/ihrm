package com.ihrm.company.controller;

import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.company.service.CompanyService;
import com.ihrm.domain.company.Company;
import com.sun.javaws.jnl.RContentDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//解决跨域问题
@CrossOrigin
@RestController
@RequestMapping("/company")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    //保存企业
    @PostMapping
    public Result save(@RequestBody Company company){
        //业务操作
        companyService.add(company);
        return new Result(ResultCode.SUCCESS);
    }
    //根据id更新企业
    /**
     * 1.方法
     * 2.请求参数
     * 3.响应
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable String id,@RequestBody Company company){
        //业务操作
        company.setId(id);
        companyService.update(company);
        return new Result(ResultCode.SUCCESS);
    }
    //根据id删除企业
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id){
        companyService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }
    //根据id查询企业
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id){
        Company company = companyService.findById(id);
        Result result = new Result(ResultCode.SUCCESS);
        result.setData(company);
        return result;
    }
    //查询全部企业
    @GetMapping()
    public Result findAll(){
        List<Company> company = companyService.findAll();
        Result result = new Result(ResultCode.SUCCESS);
        result.setData(company);
        return result;
    }
}
