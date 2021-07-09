package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.system.Permission;
import com.ihrm.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限的控制层
 */
//声明
@RestController
//2.解决跨域
@CrossOrigin
//3.设置父路径
@RequestMapping("/sys")
public class PermissionController extends BaseController {
    @Autowired
    private PermissionService permissionService;

    /**
     * 保存
     * @return
     */
    @PostMapping("/permission")
    public Result save(@RequestBody Map<String,Object> map) throws Exception {
        //1.设置service完成保存权限
        permissionService.save(map);

        //2.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id修改权限信息
     */
    @PutMapping("/permission/{id}")
    public Result update(@PathVariable String id,@RequestBody  Map<String,Object> map) throws Exception {
        //1.设置修改的权限id
        map.put("id",id);
        //2.调用service更新权限信息
        permissionService.update(map);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS);
    }
    /**
     * 查询权限列表,根据type、 enVisible、pid
     * @return
     */
    @GetMapping("/permission")
    public Result findAll(@RequestParam Map map){
        //1.查询所有的权限信息
        List<Permission> list = permissionService.findAll(map);
        System.out.println("list:"+list);

        //2.将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 根据id查询权限
     */
    @GetMapping("/permission/{id}")
    public Result findById(@PathVariable String id) throws Exception {
        Map<String, Object> map = permissionService.findById(id);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,map);
    }


    /**
     * 根据id删除权限信息
     */
    @DeleteMapping("/permission/{id}")
    public Result delete(@PathVariable String id ) throws Exception {
        //调用service删除权限信息
        permissionService.deleteById(id);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS);
    }
}
