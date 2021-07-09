package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.domain.system.response.UserResult;
import com.ihrm.domain.system.response.UserSimpleResult;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import io.jsonwebtoken.Claims;
import org.apache.poi.ss.usermodel.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//声明
@RestController
//2.解决跨域
@CrossOrigin
//3.设置父路径
@RequestMapping("/sys")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionService permissionService;
//    @Autowired
//    private JwtUtils jwtUtils;

//    @Autowiredf
//    private JwtUtils jwtUtils;
    //将DepartmentFeignClient接口注入进来
    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    /**
     * 导入excel,批量导入用户的信息
     * 文件上传:springboot
     */
    @PostMapping("/user/import")
    public Result importUser(@RequestParam MultipartFile file) throws Exception {
       /* //1.解析Excel
        //1.1创建工作簿,XSSFWorkbook 2007版本的 ，HSSFWorkbook：2003版本的
        Workbook wb=new XSSFWorkbook(file.getInputStream());
        //1.2.通过工作簿，获取Sheet表单
        Sheet sheet = wb.getSheetAt(0);//参数（索引）
        //1.3.获取Sheet表单中的每一行，和每一个单元格的内容
        //sheet.getLastRowNum()：得到最后一行的索引
        //2.获取用户数据列表
        List<User> list=new ArrayList<>();
        for (int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){//getLastRowNum() 最后一行
            //根据索引获取行
            Row row = sheet.getRow(rowNum);
            Object[] values=new Object[row.getLastCellNum()];
            //getLastCellNum() 得到最大列的数量
            for (int cellNum=1;cellNum<row.getLastCellNum();cellNum++){
                //根据索引获取每一个单元格
                Cell cell = row.getCell(cellNum);
                //获取每一个单元格的内容
                Object value = getCellValue(cell);
                //将单元格的内容保存到list数组里面
                values[cellNum]=value;
            }
            //将values数组里面的值保存到User对象中
            User user = new User(values);
            //把user对象保存到list数组中
            list.add(user);
        }*/
        //rowIndex: 读取数据的起始行，从0开始，1代表第二行；cellIndex:  读取数据的起始单元格位置，从0开始，1代表第二行
        List<User> list = new ExcelImportUtil(User.class).readExcel(file.getInputStream(), 1, 1);
        //3.批量保存用户到数据库当中
        userService.saveAll(list,companyId,companyName);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 获取每一个单元格的内容
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell){
        //1.获取到单元格的数据类型,  FORMULA：计算函数 NUMBER：日期和数字
        CellType cellType = cell.getCellType();
        //2.根据单元格数据类型获取数据
        Object value=null;
        switch (cellType){
            case STRING:
                //字符串类型
                value=cell.getStringCellValue();
                break;
            case BOOLEAN:
                //布尔类型
                value=cell.getBooleanCellValue();
                break;
            case NUMERIC:
                //NUMBERIC 代表日期和数字
                //DateUtil.isCellDateFormatted：判断是否是日期类型数据
                if(DateUtil.isCellDateFormatted(cell)){
                    //日期格式
                    value=cell.getDateCellValue();
                }else{
                    //数字类型
                    value=cell.getNumericCellValue();
                }
                break;
            case FORMULA:
                //公式类型
                value=cell.getCellFormula();
                break;
            default:
                break;
        }
        return value;
    }
    /**
     * 测试Feign组件
     * 调用系统微服务的/test接口传递部门id，通过feign调用部门微服务获取部门信息
     */
    @GetMapping("/test/{id}")
    public Result testFeign(@PathVariable String id){
        Result result = departmentFeignClient.findById(id);
        return result;
    }

    /**
     * 文件上传,上传用户的头像
     */
    @RequestMapping("/user/upload/{id}")
    public Result upload(@PathVariable String id, @RequestParam(name="file")MultipartFile file) throws IOException {
        //1.调用service保存图片（获取到图片的访问地址（dataUrl | http地址））
        String imgUrl=userService.uploadImage(id,file);

        //2.返回数据
        return new Result(ResultCode.SUCCESS,imgUrl);
    }

    /**
     * 保存
     * @return
     */
    @PostMapping("/user")
    public Result save(@RequestBody User user){
        //1.设置保存的企业id
        /**
         * 企业id：目前使用固定值1，后面解决
         */
        user.setCompanyId(companyId);
        user.setCompanyName(companyName);
        user.setInServiceStatus(1);
        //2.设置service完成保存企业
        userService.save(user);

        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询企业的用户列表
     * 指定企业id
     * @return
     */
    @GetMapping("/user")
    public Result findAll(int size,int page,@RequestParam Map map){
        //1.设置保存的企业id
        map.put("companyId",companyId);
        //2.根据公司id查询所有的用户信息
        Page<User> pageUser = userService.findAll(map,page,size);
        List<User> users=new ArrayList<>();
        for (User user : pageUser) {
            users.add(user);
        }
        //3.构造返回结果(总条数，数据列表)
        PageResult<User> pageResult = new PageResult<User>(pageUser.getTotalElements(),pageUser.getContent());

        Map m = userService.findUserStatus();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("data",pageResult);
        map1.put("userIn",m.get("userIn"));
        map1.put("userOut",m.get("userOut"));
        map1.put("userTotal",m.get("userTotal"));
        System.out.println("map1:"+map1);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,map1);
    }

    /**
     * 根据id查询用户
     */
    @GetMapping("/user/{id}")
    public Result findById(@PathVariable String id ){
        // 添加 roleIds (用户已经具有的角色id数组)
        User user = userService.findById(id);

        UserResult userResult = new UserResult(user);

        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS,userResult);
    }

    /**
     * 根据公司编号，查询用户的id和用户的名称
     * @return
     * @throws Exception
     */
    @GetMapping("/user/simple")
    public Result simple() throws Exception {
        List<UserSimpleResult> list = new ArrayList<>();
        List<User> users = userService.findAll(companyId);
        for (User user : users) {
            list.add(new UserSimpleResult(user.getId(),user.getUsername()));
        }
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 根据id修改用户信息
     */
    @PutMapping("/user/{id}")
    public Result update(@PathVariable String id,@RequestBody  User user ){
        //1.设置修改的用户id
        user.setId(id);
        //2.调用service更新用户信息
        userService.update(user);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除用户信息
     */
    @RequiresPermissions(value = "API-USER-DELETE")
    @DeleteMapping("/user/{id}")
    public Result delete(@PathVariable String id ){
        //调用service删除用户信息
        userService.deleteById(id);
        //将结果返回给我们的前端
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 分配角色
     */
    @PutMapping("/user/assignRoles")
    public Result assignRoles(@RequestBody Map<String,Object> map){
        //1.获取被分配的用户id
        String userId = (String)map.get("id");
        //2.获取到角色的id列表
        List<String> roleIds=(List<String>) map.get("roleIds");
        //3.调用service完成角色分配
        userService.assignRoles(userId,roleIds);
        //返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 用户登录
     *  1.通过service根据mobile查询用户
     *  2.比较password
     *  3.生成jwt信息
     *
     */
   @RequestMapping(value="/login",method = RequestMethod.POST)
    public Result login(@RequestBody Map<String,String> loginMap) {
        String mobile = loginMap.get("mobile");
        String password = loginMap.get("password");
        try {
            //1.构造登录令牌 UsernamePasswordToken
            //加密密码
            password = new Md5Hash(password,mobile,3).toString();  //1.密码，盐，加密次数
            UsernamePasswordToken upToken = new UsernamePasswordToken(mobile,password);
            //2.获取subject
            Subject subject = SecurityUtils.getSubject();
            //3.调用login方法，进入realm完成认证
            subject.login(upToken);
            //4.获取sessionId
            String sessionId = (String)subject.getSession().getId();
            //5.构造返回结果
            return new Result(ResultCode.SUCCESS,sessionId);
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }
    }


    /**
     * 用户登录成功之后，获取用户信息
     *      1.获取用户id
     *      2.根据用户id查询用户
     *      3.构建返回值对象
     *      4.响应
     */
    @RequestMapping(value="/profile",method = RequestMethod.POST)
    public Result profile(HttpServletRequest request) throws Exception {
        //获取session中的安全数据
        Subject subject = SecurityUtils.getSubject();
        //1.subject获取所有的安全数据集合
        PrincipalCollection principals = subject.getPrincipals();
        //2.获取安全数据
        ProfileResult result = (ProfileResult)principals.getPrimaryPrincipal();

//        String userid = claims.getId();
//        //获取用户信息
//        User user = userService.findById(userid);
//        //根据不同的用户级别获取用户权限
//
//        ProfileResult result = null;
//
//        if("user".equals(user.getLevel())) {
//            result = new ProfileResult(user);
//        }else {
//            Map map = new HashMap();
//            if("coAdmin".equals(user.getLevel())) {
//                map.put("enVisible","1");
//            }
//            List<Permission> list = permissionService.findAll(map);
//            result = new ProfileResult(user,list);
//        }
        return new Result(ResultCode.SUCCESS,result);
    }

   /**
     * 根据mobile查询用户
     */
   /* @PostMapping("/login")
    public Result login(@RequestBody Map<String,String> loginMap){
        String mobile=loginMap.get("mobile");
        String password=loginMap.get("password");
        User user= userService.findByMobile(mobile);
        System.out.println("user:"+user);
        //判断验证密码不正确，根据手机号码找不到用户
        if(user==null || !user.getPassword().equals(password)){
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }else {
            //登录成功
            Map<String,Object> map=new HashMap<>();
            map.put("companyId",user.getCompanyId());
            map.put("companyName",user.getCompanyName());
            String token = jwtUtils.createJwt(user.getId(), user.getUsername(), map);
            return new Result(ResultCode.SUCCESS,token);
        }
    }

    @RequestMapping(value="/profile",method = RequestMethod.POST)
    public Result profile(HttpServletRequest request) throws Exception{
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            throw new CommonException(ResultCode.UNAUTHENTICATED);
        }
        String token=authorization.replace("Bearer ","");
        Claims claims = jwtUtils.parseJwt(token);
        String userid=claims.getId();
        User user = userService.findById(userid);

        //根据不同的用户级别获取用户权限
        //1.saas平台管理员具有所有的权限
        //2.企业管理员具有所有的企业权限
        ProfileResult result=null;
        if("user".equals(user.getLevel())){
            result=new ProfileResult(user);
        }else {
            Map map = new HashMap<>();
            //企业管理员
            if("coAdmin".equals(user.getLevel())){
                map.put("enVisible","1");
            }
            List<Permission> list = permissionService.findAll(map);
            result=new ProfileResult(user,list);
        }
        return new Result(ResultCode.SUCCESS,result);
    }*/
}
