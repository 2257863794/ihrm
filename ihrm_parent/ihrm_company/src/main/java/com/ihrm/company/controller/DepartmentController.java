package com.ihrm.company.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.company.dao.DepartmentDao;
import com.ihrm.company.service.CompanyService;
import com.ihrm.company.service.DepartmentService;
import com.ihrm.domain.company.Company;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.company.response.DeptListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//2.解决跨域
@CrossOrigin
//3.设置父路径
@RequestMapping("/company")
public class DepartmentController extends BaseController {
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private CompanyService companyService;

    /**
     * 保存
     * @return
     */
    @PostMapping("/department")
    public Result save(@RequestBody Department department){
        //1.设置保存的企业id
        /**
         * 企业id：目前使用固定值1，后面解决
         */
       // String companyId="1";
        department.setCompanyId(companyId);
        //2.设置service完成保存企业
        departmentService.save(department);

        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询企业的部门列表
     * 指定企业id
     * @return
     */
    @GetMapping("/department")
    public Result findAll(){
        //1.设置保存的企业id
       // String companyId="1";
            //根据公司编号查询出公司的信息
        Company company= companyService.findById(companyId);
        //2.根据公司id查询所有的部门信息
        List<Department> depts = departmentService.findAll(companyId);
        //3.构造返回结果
        DeptListResult deptListResult = new DeptListResult(company,depts);
        System.out.println(deptListResult);
        System.out.println("公司id:"+companyId);
        System.out.println(ObjectUtils.isEmpty(companyId));

        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,deptListResult);
    }

    /**
     * 根据id查询部门
     */
    @GetMapping("/department/{id}")
    public Result findById(@PathVariable String id ){
        Department department = departmentService.findById(id);
        System.out.println(companyId);
        System.out.println("公司id:"+companyId);
        System.out.println(ObjectUtils.isEmpty(companyId));
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,department);
    }

    /**
     * 根据id修改部门信息
     */
    @PutMapping("/department/{id}")
    public Result update(@PathVariable String id,@RequestBody  Department department ){
        //1.设置修改的部门id
        department.setId(id);
        //2.调用service更新部门信息
        departmentService.update(department);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除部门信息
     */
    @DeleteMapping("/department/{id}")
    public Result delete(@PathVariable String id ){
        //调用service删除部门信息
        departmentService.deleteById(id);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS);
    }

    /**
     *根据部门编码和企业id查询部门
     */
    @PostMapping("/department/search")
    public Department findByCode(@RequestParam String code,@RequestParam String companyId){
        Department dept=departmentService.findByCode(code,companyId);
        return dept;
    }
}
