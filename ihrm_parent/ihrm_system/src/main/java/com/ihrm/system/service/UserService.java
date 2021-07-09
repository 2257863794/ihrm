package com.ihrm.system.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.util.CopyUtil;
import com.ihrm.common.util.IdWorker;
import com.ihrm.common.util.QiniuUploadUtil;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.dao.RoleDao;
import com.ihrm.system.dao.UserDao;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.bouncycastle.jcajce.provider.digest.MD5;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;

@Service
public class UserService extends BaseService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    /**
     * 查找用户的在职状态
     * @return
     */
    public Map findUserStatus(){
        return userDao.findUserStatus();
    }

    /**
     * 根据mobile查询用户
     */
    public User findByMobile(String mobile){
        return userDao.findByMobile(mobile);
    }
    /**
     * 批量保存用户
     */
    @Transactional
    public void saveAll(List<User> list,String companyId,String companyName){
        for (User user : list) {
            //需要设置默认密码，user.getMobile()：加盐，3：加密次数
            user.setPassword(new Md5Hash("123456",user.getMobile(),3).toString());
            //需要设置id
            user.setId(idWorker.nextId()+"");
            //需要设置基本属性
            user.setCompanyId(companyId);
            user.setCompanyName(companyName);
            user.setInServiceStatus(1);//是否是一个正常的入职状态，1.在职
            user.setEnableState(1);//当前用户是否可以使用，是否可以登录系统，1为启用
            user.setLevel("user");//当前用户的级别

            //填充部门的属性
            Department department = departmentFeignClient.findByCode(user.getDepartmentId(), companyId);
            if(department!=null){
                user.setDepartmentId(department.getId());
                user.setDepartmentName(department.getName());
            }
            userDao.save(user);
        }
    }

    /**
     * 完成图片处理
     *  id:用户id
     *  file：用户上传的头像文件
     *  return:请求路径
     *  dataURL形式
     */
    public String uploadImage(String id, MultipartFile file) throws IOException {
        //1.根据id查询用户
        User user = userDao.findById(id).get();
        //2.使用DataUrl的形式存储图片（对图片Byte数组进行base64编码）
        //Base64.encode 把文件进行base64位字符编码
        String encode="data:image/png;base64,"+ Base64.encode(file.getBytes());
        System.out.println(encode);
        //3.更新用户头像地址
        user.setStaffPhoto(encode);
        userDao.save(user);
        //4.返回
        return encode;

    }

    /**
     * 上传到七牛云存储
     * @param id
     * @param file
     * @return
     * @throws IOException
     */
   /* public String uploadImage(String id, MultipartFile file) throws IOException {
        //1.根据id查询用户
        User user = userDao.findById(id).get();
        //2.将图片上传到七牛云存储，获取请求路径
        String imgUrl = new QiniuUploadUtil().upload(user.getId(), file.getBytes());
        System.out.println(imgUrl);
        //3.更新用户头像地址
        user.setStaffPhoto(imgUrl);
        userDao.save(user);
        //4.返回
        return imgUrl;

    }*/

    /**
     * 1、保存用户
     * 1.配置idwork到工程
     * 2.在service配置
     * 3.通过idwork生成id
     * 4.保存用户
     */
    public void save(User user){
        String id=idWorker.nextId()+"";
        user.setId(id);
        user.setPassword("123456");//初始化密码
        user.setEnableState(1);//1启用
        //调用dao保存用户
        userDao.save(user);
    }

    /**
     * 2、更新用户
     *  1.参数：User
     *  2.先查询再更新，根据id去查询用户对象
     *  3.设置dao完成更新操作
     */
    public void update(User user){
        //1.根据id查询用户
        User target=userDao.findById(user.getId()).get();

        BeanUtils.copyProperties(user,target);
        System.out.println("target基础信息:"+target);

        //2.设置用户属性
        target.setUsername(user.getUsername());
        target.setPassword(user.getPassword());
        target.setDepartmentId(user.getDepartmentId());
        target.setDepartmentName(user.getDepartmentName());

       // User user1 = CopyUtil.copy(user, User.class);

       // BeanUtils.copyProperties(user,target);
        //3.更新用户
        userDao.save(target);
    }

    /**
     * 3、根据id查询用户
     */
    public User findById(String id){
        return userDao.findById(id).get();
    }

    /**
     * 查询该公司某一个部门下面的全部用户列表
     */
    public List<User> findAll(String companyId){
        return userDao.findAll(super.getSpec(companyId));
    }

    /**
     * 4、查询该公司某一个部门下面的全部用户列表,分页查询
     *      参数：map集合的形式
     *          hasDept
     *          departmentId
     *          companyId
     */
    public Page<User> findAll(Map<String,Object> map,int page,int size){
        //1.需要查询条件
        Specification<User> spec=new Specification<User>() {
            /**
             * 动态拼接查询条件
             * @param root  包含了所有的对象属性
             * @param criteriaQuery 一般不用
             * @param cb  ：构造查询条件
             * @return*/

            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                //根据请求的companyId是否为空构造查询条件
                if(!StringUtils.isEmpty(map.get("companyId"))){// companyId=companyId
                    list.add(cb.equal(root.get("companyId").as(String.class),map.get("companyId")));
                }
                //根据请求的部门id是否为空构造查询条件
                if(!StringUtils.isEmpty(map.get("departmentId"))){// departmentId=null
                    list.add(cb.equal(root.get("departmentId").as(String.class),map.get("departmentId")));
                }
                //根据请求的hasDept判断 是否分配部门 0未分配（departmentId=null） 1分配（departmentId！=null）
               /* if(StringUtils.isEmpty(map.get("departmentId")) || "0".equals(map.get("hasDept"))){
                    list.add(cb.isNull(root.get("departmentId")));// departmentId=null
                }else{
                    list.add(cb.isNotNull(root.get("departmentId")));// departmentId!=null
                }*/
                if(!StringUtils.isEmpty(map.get("hasDept"))) {
                    //根据请求的hasDept判断  是否分配部门 0未分配（departmentId = null），1 已分配 （departmentId ！= null）
                    if("0".equals((String) map.get("hasDept"))) {
                        list.add(cb.isNull(root.get("departmentId")));
                    }else {
                        list.add(cb.isNotNull(root.get("departmentId")));
                    }
                }
                //根据请求的在职状态是否为空构造查询条件
                if(!StringUtils.isEmpty(map.get("stausInfo"))){// departmentId=null
                    list.add(cb.equal(root.get("inServiceStatus"),map.get("stausInfo")));
                }
               return cb.and(list.toArray(new Predicate[list.size()]));//把list转换为数组

                //root.get("companyId").as(String.class) ：构造属性，  companyId ：属性值
                //构造查询条件，如： company_id=companyId
            }
        };

        //2.分页
        //findAll(查询条件，分页条件)，page默认是从0开始，我们传递的是1和2，所有需要减一
        Page<User> pageUser= userDao.findAll(spec, PageRequest.of(page-1, size));//把查询条件传递进去,page当前页，size当前页的记录条数
        return pageUser;
    }
    /**
     * 5、删除用户
     */
    public void deleteById(String id){
        userDao.deleteById(id);
    }

    /**
     * 6.分配角色
     */
    public void assignRoles(String userId,List<String> roleIds){
        //1.根据id查询用户,用户表
        User user = userDao.findById(userId).get();
        //2.设置用户的角色集合，set集合无序的
        Set<Role> roles=new HashSet<>();
        //循环角色id,通过角色id得到角色对象，并保存到集合中
        for (String roleId : roleIds) {
            Role role = roleDao.findById(roleId).get();
            roles.add(role);
        }
        //3、将角色集合设置给用户,将角色集合保存到对应的角色表里面，bs_user 与 per_role是多对多关系，会进行绑定的
        user.setRoles(roles);

        //4.更新用户,将用户保存进数据库
        userDao.save(user);
    }
}
