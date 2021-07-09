package com.ihrm.system.service;

import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.service.BaseService;
import com.ihrm.common.util.BeanMapUtils;
import com.ihrm.common.util.IdWorker;
import com.ihrm.common.util.PermissionConstants;
import com.ihrm.domain.system.*;
import com.ihrm.system.dao.*;
import org.springframework.beans.BeanUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 权限操作，业务逻辑层
 */
@Service
public class PermissionService{
    //权限dao层
    @Autowired
    private PermissionDao permissionDao;
    //权限菜单dao层
    @Autowired
    private PermissionMenuDao permissionMenuDao;
    //权限按钮dao层
    @Autowired
    private PermissionPointDao permissionPointDao;
    //权限api的dao层
    @Autowired
    private PermissionApiDao permissionApiDao;
    @Autowired
    private IdWorker idWorker;

    /**
     * 1、保存权限
     *
     */
    public void save(Map<String,Object> map) throws Exception {
        //权限表【有一个type字段来区分是那个资源】包含资源【菜单表、按钮表、api接口表】
        //1.设置主键的值
        String id=idWorker.nextId()+"";
        //2.通过map构造permission对象,通过工具类BeanMapUtils，把map集合转换为一个对象
        Permission perm = BeanMapUtils.mapToBean(map, Permission.class);
        perm.setId(id);//给这个对象设置id属性值

        //3.根据类型构造不同的资源对象（菜单、按钮、api接口）
        int type=perm.getType();
        switch (type){
            //菜单资源对象
            case PermissionConstants.PERMISSION_MENU:
                PermissionMenu menu=BeanMapUtils.mapToBean(map,PermissionMenu.class);
                menu.setId(id);
                permissionMenuDao.save(menu);
                break;
             //按钮资源对象
            case PermissionConstants.PERMISSION_POINT:
                PermissionPoint point=BeanMapUtils.mapToBean(map,PermissionPoint.class);
                point.setId(id);
                permissionPointDao.save(point);
                break;
            //api接口资源对象
            case PermissionConstants.PERMISSION_API:
                PermissionApi api=BeanMapUtils.mapToBean(map, PermissionApi.class);
                api.setId(id);
                permissionApiDao.save(api);
                break;
            default:
                throw new CommonException(ResultCode.FAIL);
        }
        //调用dao保存权限
        permissionDao.save(perm);
    }

    /**
     * 2、更新权限
     *  1.参数：Role
     *  2.先查询再更新，根据id去查询权限对象
     *  3.设置dao完成更新操作
     */
   public void update(Map<String,Object> map) throws Exception {
       //封装为一个权限对象
       Permission perm = BeanMapUtils.mapToBean(map, Permission.class);
       //1.通过传递的权限id查询权限
       Permission permission = permissionDao.findById(perm.getId()).get();
       //设置值
       permission.setName(perm.getName());
       permission.setCode(perm.getCode());
       permission.setDescription(perm.getDescription());
       permission.setEnVisible(perm.getEnVisible());

       //BeanUtils.copyProperties(perm,permission);

       //3.根据类型构造不同的资源对象（菜单、按钮、api接口）
       int type=perm.getType();
       switch (type) {
           //菜单资源对象
           case PermissionConstants.PERMISSION_MENU:
               PermissionMenu menu = BeanMapUtils.mapToBean(map, PermissionMenu.class);
               menu.setId(perm.getId());
               permissionMenuDao.save(menu);
               break;
           //按钮资源对象
           case PermissionConstants.PERMISSION_POINT:
               PermissionPoint point = BeanMapUtils.mapToBean(map, PermissionPoint.class);
               point.setId(perm.getId());
               permissionPointDao.save(point);
               break;
           //api接口资源对象
           case PermissionConstants.PERMISSION_API:
               PermissionApi api = BeanMapUtils.mapToBean(map, PermissionApi.class);
               api.setId(perm.getId());
               permissionApiDao.save(api);
               break;
           default:
               throw new CommonException(ResultCode.FAIL);
       }

       //4.保存
       permissionDao.save(permission);
    }

    /**
     * 3、根据id查询权限
     *      1、查询权限
     *      2.根据权限的类型查询资源
     *      3.构造map集合
     */
    public Map<String,Object>  findById(String id) throws Exception {
        //通过id查询权限信息
        Permission perm = permissionDao.findById(id).get();
        //得到权限的类型
        int type=perm.getType();
        //构造Map集合
        Object object=null;
        if(type == PermissionConstants.PERMISSION_MENU){//菜单
            object=permissionMenuDao.findById(id).get();
        }else if(type == PermissionConstants.PERMISSION_POINT){//按钮
            object=permissionPointDao.findById(id).get();
        }else if(type == PermissionConstants.PERMISSION_API){//api
            object=permissionApiDao.findById(id).get();
        }else{
            throw new CommonException(ResultCode.FAIL);
        }
        Map<String, Object> map = BeanMapUtils.beanToMap(object);
        //给集合中的属性赋值
        map.put("name",perm.getName());
        map.put("type",perm.getType());
        map.put("code",perm.getCode());
        map.put("description",perm.getDescription());
        map.put("pid",perm.getPid());
        map.put("enVisible",perm.getEnVisible());
        //返回map集合
        return map;
    }

    /**
     * 4、查询全部
     *  type：查询全部权限列表 type=0，菜单+按钮（权限点）；type=1,菜单; type=2,按钮（权限点）；type=3,api接口
     *  enVisible：0【查询所有saas平台的最高权限】，1【查询企业的权限】
     *  pid：父id
     */
    public List<Permission> findAll(Map<String,Object> map){
        //1.需要查询条件
        Specification<Permission> spec=new Specification<Permission>() {
            //动态拼接查询条件
            @Override
            public Predicate toPredicate(Root<Permission> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                //根据父id查询
                if(!StringUtils.isEmpty(map.get("pid"))){
                    list.add(cb.equal(root.get("pid").as(String.class),(String)map.get("pid")));
                }
                //根据enVisible查询,enVisible=?
                if(!StringUtils.isEmpty(map.get("enVisible"))){
                    list.add(cb.equal(root.get("enVisible").as(String.class),(String)map.get("enVisible")));
                }
                //根据类型type查询,in(1,2,3)
                if(!StringUtils.isEmpty(map.get("type"))){
                    String type=map.get("type")+"";
                    CriteriaBuilder.In<Object> in = cb.in(root.get("type"));
                    if("0".equals(type)){
                        in.value(1).value(2);
                    }else{
                        in.value(Integer.valueOf(type));
                    }
                    list.add(in);
                }
                //把构造的的条件list返回
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return permissionDao.findAll(spec);
    }

    /**
     * 5、根据id删除
     *  1.删除权限
     *  2.删除权限对应的资源
     */
    public void deleteById(String id) throws Exception {
        //1.通过传递的id查询权限
        Permission permission = permissionDao.findById(id).get();
        //删除该权限
        permissionDao.delete(permission);

        //2.根据类型资源不同，去删除对应的资源
        int type=permission.getType();


        switch (type){
            //删除菜单资源
            case PermissionConstants.PERMISSION_MENU:
                permissionMenuDao.deleteById(id);

                List<Permission> permissions = permissionDao.findByPid(id);
                permissionDao.deleteAll(permissions);
                for (Permission permission1 : permissions) {
                    int tp = permission1.getType();
                    switch (tp){
                        //删除按钮资源
                        case PermissionConstants.PERMISSION_POINT:
                            System.out.println("point删除");
                            permissionPointDao.deleteById(permission1.getId());
                            break;
                        case PermissionConstants.PERMISSION_API:
                            System.out.println("api删除");
                            permissionApiDao.deleteById(permission1.getId());
                            break;
                    }
                }
                permissionDao.deleteAll(permissions);
                break;
                //删除按钮资源
            case PermissionConstants.PERMISSION_POINT:
                permissionPointDao.deleteById(id);
                break;
            case PermissionConstants.PERMISSION_API:
                permissionApiDao.deleteById(id);
                break;
            default:
                throw new CommonException(ResultCode.FAIL);
        }
    }

}
