package com.ihrm.system.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.util.IdWorker;
import com.ihrm.common.util.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.system.dao.PermissionDao;
import com.ihrm.system.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * 角色操作业务逻辑层
 */
@Service
public class RoleService extends BaseService {
    @Autowired
    private RoleDao roleDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PermissionDao permissionDao;//权限

    /**
     * 给角色分配权限（角色id,权限id）
     */
    public void assignPerms(String roleId,List<String> permIds){
        //1.获取分配的角色对象
        Role role = roleDao.findById(roleId).get();
        //2.构造角色的权限集合
        Set<Permission> perms = new HashSet<>();
        for (String permId : permIds) {
            //根据权限id获取权限对象
            Permission permission=permissionDao.findById(permId).get();
            //需要根据父id和类型查询API权限列表
            List<Permission> apiList=permissionDao.findByTypeAndPid(PermissionConstants.PERMISSION_API,permission.getId());
            //将API的权限列表保存到权限集合中去
            perms.addAll(apiList);
            //将当前菜单或者按钮权限保存到权限集合中去
            perms.add(permission);
        }
        System.out.println(perms.size());
        //3.将权限集合赋值给角色
        role.setPermissions(perms);
        //4.更新角色
        roleDao.save(role);
    }

    /**
     * 1、添加角色
     *
     */
    public void save(Role urole){
        String id=idWorker.nextId()+"";
        urole.setId(id);
        
        //调用dao保存角色
        roleDao.save(urole);
    }

    /**
     * 2、更新角色
     *  1.参数：Role
     *  2.先查询再更新，根据id去查询角色对象
     *  3.设置dao完成更新操作
     */
    public void update(Role role){
        //1.根据id查询角色
        Role target=roleDao.getOne(role.getId());

        //2.设置角色属性
        target.setDescription(role.getDescription());
        target.setName(role.getName());

       // BeanUtils.copyProperties(urole,target);
        //3.更新角色
        roleDao.save(target);
    }

    /**
     * 3、根据id查询角色
     */
    public Role findById(String id){
        return roleDao.findById(id).get();
    }

    /**
     * 4、查询该公司下面的全部角色列表，分页
     */
    public Page<Role> findAllByPage(String companyId,int page,int size){
        //2.分页
        //findAll(查询条件，分页条件)，page默认是从0开始，我们传递的是1和2，所有需要减一
        Page<Role> pageRole= roleDao.findAll(getSpec(companyId), PageRequest.of(page-1, size));//把查询条件传递进去,page当前页，size当前页的记录条数
        return pageRole;
    }

    /**
     * 5、根据公司id，查询全部的角色列表
     * @param companyId
     * @return
     */
    public List<Role> findAll(String companyId){
        return roleDao.findAll(getSpec(companyId));
    }
    /**
     * 6、删除角色
     */
    public void deleteById(String id){
        roleDao.deleteById(id);
    }
}
