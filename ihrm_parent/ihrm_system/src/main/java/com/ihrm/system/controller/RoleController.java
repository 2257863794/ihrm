package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.system.Role;
import com.ihrm.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//声明
@RestController
//2.解决跨域
@CrossOrigin
//3.设置父路径
@RequestMapping("/sys")
public class RoleController extends BaseController {
    @Autowired
    private RoleService roleService;

    /**
     * 分配权限
     */
    @PutMapping("/role/assignPrem")
    public Result assignPerm(@RequestBody Map<String,Object> map){
        //1.从map集合中获取被分配的角色的id
        String roleId=(String) map.get("id");
        //2.获取权限的id列表
        List<String> permIds = (List<String>)map.get("permIds");
        //3.调用service完成权限分配
        roleService.assignPerms(roleId,permIds);

        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 添加角色
     * @return
     */
    @PostMapping("/role")
    public Result save(@RequestBody Role role){
        //1.设置保存的企业id
        role.setCompanyId(companyId);
        //2.设置service完成保存企业
        roleService.save(role);

        //3.构造返回结果
        //new Result(ResultCode.SUCCESS)  === Result.SUCCESS()
        return Result.SUCCESS();
    }

    /**
     * 根据id修改角色信息
     */
    @PutMapping("/role/{id}")
    public Result update(@PathVariable String id,@RequestBody  Role role ){
        //2.调用service更新角色信息
        roleService.update(role);
        //将结果返回给我们的前端
        return Result.SUCCESS();
    }

    /**
     * 根据id删除角色信息
     */
    @DeleteMapping("/role/{id}")
    public Result delete(@PathVariable String id ){
        //调用service删除角色信息
        roleService.deleteById(id);
        //将结果返回给我们的前端
        return Result.SUCCESS();
    }

    /**
     * 根据id查询角色
     */
    @GetMapping("/role/{id}")
    public Result findById(@PathVariable String id ){
        Role role = roleService.findById(id);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,role);
    }
    /**
     * 分页查询企业的角色列表
     * 指定企业id
     * @return
     */
    @GetMapping("/role")
    public Result findAllByPage(int page,int pageSize,Role role){
        System.out.println(pageSize+"==="+page);
        //2.根据公司id查询所有的角色信息
        Page<Role> pageRole = roleService.findAllByPage(companyId,page,pageSize);
        //3.构造返回结果(总条数，数据列表)
        PageResult pageResult = new PageResult(pageRole.getTotalElements(),pageRole.getContent());
        System.out.println("角色列表："+pageResult);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,pageResult);
    }

    /**
     * 查询所有的角色
     */
    @GetMapping("/role/list")
    public Result findAll(){
        List<Role> roleList = roleService.findAll(companyId);

        return new Result(ResultCode.SUCCESS,roleList);
    }



}
