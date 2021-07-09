package com.ihrm.company.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.util.CopyUtil;
import com.ihrm.common.util.IdWorker;
import com.ihrm.company.dao.DepartmentDao;
import com.ihrm.company.dao.DepartmentDao;
import com.ihrm.domain.company.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
public class DepartmentService extends BaseService {
    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private IdWorker idWorker;

    /**
     * 1、保存部门
     * 1.配置idwork到工程
     * 2.在service配置
     * 3.通过idwork生成id
     * 4.保存部门
     */
    public void save(Department department){
        String id=idWorker.nextId()+"";
        department.setId(id);

        //调用dao保存部门
        departmentDao.save(department);
    }

    /**
     * 2、更新部门
     *  1.参数：Department
     *  2.先查询再更新，根据id去查询部门对象
     *  3.设置dao完成更新操作
     */
    public void update(Department department){
        //1.根据id查询部门
        Department dept=departmentDao.findById(department.getId()).get();

        //2.设置部门属性
        Department dept1 = CopyUtil.copy(department, Department.class);

        //3.更新部门
        departmentDao.save(dept1);
    }

    /**
     * 3、根据id查询部门
     */
    public Department findById(String id){
        return departmentDao.findById(id).get();
    }
    /**
     * 4、查询某个公司的所有的部门列表
     */
    public List<Department> findAll(String companyId){

        /*Specification<Department> spec=new Specification<Department>() {
            *//**
             * 用于构造查询条件
             * @param root  包含了所有的对象属性
             * @param criteriaQuery 一般不用
             * @param cb  ：构造查询条件
             * @return
             *//*
            @Override
            public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
               //root.get("companyId").as(String.class) ：构造属性，  companyId ：属性值
                //构造查询条件，如： company_id=companyId
                return cb.equal(root.get("companyId").as(String.class),companyId);
            }
        };*/
        return departmentDao.findAll(getSpec(companyId));//把查询条件传递进去
    }
    /**
     * 5、删除部门
     */
    public void deleteById(String id){
        departmentDao.deleteById(id);
    }

    /**
     * 根据部门编码和企业id查询部门
     */
    public Department findByCode(String code,String companyId){
        return departmentDao.findByCodeAndCompanyId(code,companyId);
    }
}
