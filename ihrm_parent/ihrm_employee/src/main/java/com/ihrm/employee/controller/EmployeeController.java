package com.ihrm.employee.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.poi.ExcelExportUtil;
import com.ihrm.common.util.BeanMapUtils;
import com.ihrm.common.util.DownloadUtils;
import com.ihrm.domain.employee.*;
import com.ihrm.domain.employee.response.EmployeeReportResult;
import com.ihrm.domain.system.User;
import com.ihrm.employee.service.*;
import net.sf.jasperreports.engine.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
@CrossOrigin
@RequestMapping("/employees")
public class EmployeeController extends BaseController {
    @Autowired
    private UserCompanyPersonalService userCompanyPersonalService;
    @Autowired
    private UserCompanyJobsService userCompanyJobsService;
    @Autowired
    private ResignationService resignationService;
    @Autowired
    private TransferPositionService transferPositionService;
    @Autowired
    private PositiveService positiveService;//员工转正
    @Autowired
    private ArchiveService archiveService;//员工归档


    /**
     * 打印员工pdf报表x
     */
    @RequestMapping(value="/{id}/pdf",method = RequestMethod.GET)
    public void pdf(@PathVariable String id) throws IOException {
        //1.引入jasper文件
        Resource resource = new ClassPathResource("templates/profile.jasper");
        FileInputStream fis = new FileInputStream(resource.getFile());

        //2.构造数据
        //a.用户详情数据
        UserCompanyPersonal personal = userCompanyPersonalService.findById(id);
        //b.用户岗位信息数据
        UserCompanyJobs jobs = userCompanyJobsService.findById(id);

        User user = userCompanyPersonalService.findStaffPhotoById(id);

        byte [] imagedata = DatatypeConverter.parseBase64Binary(user.getStaffPhoto().substring(user.getStaffPhoto().indexOf(",")+ 1));
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
        File img=new File("img.png");
        //把bufferedImage里面的对象写到，img文件里面
        ImageIO.write(bufferedImage,"png",img);
        //把img文件转换为一个uri地址
        personal.setStaffPhoto(img.toURI().toString());
        System.out.println("用户头像："+user.getStaffPhoto());
        //c.用户头像        域名 / id
       String staffPhoto = personal.getStaffPhoto();

       // System.out.println(staffPhoto);


        //3.填充pdf模板数据，并输出pdf
        Map params = new HashMap();

        Map<String, Object> map1 = BeanMapUtils.beanToMap(personal);
        Map<String, Object> map2 = BeanMapUtils.beanToMap(jobs);

        params.putAll(map1);
        params.putAll(map2);
        params.put("staffPhoto",staffPhoto);

        ServletOutputStream os = response.getOutputStream();
        try {
            JasperPrint print = JasperFillManager.fillReport(fis, params,new JREmptyDataSource());
            JasperExportManager.exportReportToPdfStream(print,os);
        } catch (JRException e) {
            e.printStackTrace();
        }finally {
            os.flush();
        }
    }

    /**
     * 员工个人信息保存
     */
    @RequestMapping(value = "/{id}/personalInfo", method = RequestMethod.PUT)
    public Result savePersonalInfo(@PathVariable(name = "id") String uid, @RequestBody Map map) throws Exception {
        UserCompanyPersonal sourceInfo = BeanMapUtils.mapToBean(map, UserCompanyPersonal.class);
        if (sourceInfo == null) {
            sourceInfo = new UserCompanyPersonal();
        }
        sourceInfo.setUserId(uid);
        sourceInfo.setCompanyId(super.companyId);
        System.out.println("个人信息："+sourceInfo);
        userCompanyPersonalService.save(sourceInfo);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 员工个人信息读取
     */
    @RequestMapping(value = "/{id}/personalInfo", method = RequestMethod.GET)
    public Result findPersonalInfo(@PathVariable(name = "id") String uid) throws Exception {
        UserCompanyPersonal info = userCompanyPersonalService.findById(uid);
        if(info == null) {
            info = new UserCompanyPersonal();
            info.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS,info);
    }

    /**
     * 员工岗位信息保存
     */
    @RequestMapping(value = "/{id}/jobs", method = RequestMethod.PUT)
    public Result saveJobsInfo(@PathVariable(name = "id") String uid, @RequestBody UserCompanyJobs sourceInfo) throws Exception {

        //更新员工岗位信息
        if (sourceInfo == null) {
            sourceInfo = new UserCompanyJobs();
        }
        sourceInfo.setUserId(uid);
        sourceInfo.setCompanyId(super.companyId);

        System.out.println("员工岗位信息："+sourceInfo);
        userCompanyJobsService.save(sourceInfo);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 员工岗位信息读取
     */
    @RequestMapping(value = "/{id}/jobs", method = RequestMethod.GET)
    public Result findJobsInfo(@PathVariable(name = "id") String uid) throws Exception {
        UserCompanyJobs info = userCompanyJobsService.findById(uid);
        if(info == null) {
            info = new UserCompanyJobs();
            info.setUserId(uid);
            info.setCompanyId(companyId);
        }
        return new Result(ResultCode.SUCCESS,info);
    }

    /**
     * 离职表单保存
     */
    @RequestMapping(value = "/{id}/leave", method = RequestMethod.PUT)
    public Result saveLeave(@PathVariable(name = "id") String uid, @RequestBody EmployeeResignation resignation) throws Exception {
        resignation.setUserId(uid);
        resignationService.save(resignation);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 离职表单读取
     */
    @RequestMapping(value = "/{id}/leave", method = RequestMethod.GET)
    public Result findLeave(@PathVariable(name = "id") String uid) throws Exception {
        EmployeeResignation resignation = resignationService.findById(uid);
        if(resignation == null) {
            resignation = new EmployeeResignation();
            resignation.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS,resignation);
    }

    /**
     * 导入员工
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public Result importDatas(@RequestParam(name = "file") MultipartFile attachment) throws Exception {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 调岗表单保存
     */
    @RequestMapping(value = "/{id}/transferPosition", method = RequestMethod.PUT)
    public Result saveTransferPosition(@PathVariable(name = "id") String uid, @RequestBody EmployeeTransferPosition transferPosition) throws Exception {
        transferPosition.setUserId(uid);
        transferPositionService.save(transferPosition);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 调岗表单读取
     */
    @RequestMapping(value = "/{id}/transferPosition", method = RequestMethod.GET)
    public Result findTransferPosition(@PathVariable(name = "id") String uid) throws Exception {
      // UserCompanyJobs jobsInfo = userCompanyJobsService.findById(uid);
        //调用调岗申请业务层方法，获取员工的信息
        EmployeeTransferPosition jobsInfo = transferPositionService.findById(uid);
        if(jobsInfo == null) {
            jobsInfo = new EmployeeTransferPosition();
            jobsInfo.setUserId(uid);
        }
        System.out.println("员工调岗信息："+jobsInfo);
        return new Result(ResultCode.SUCCESS,jobsInfo);
    }

    /**
     * 转正表单保存
     */
    @RequestMapping(value = "/{id}/positive", method = RequestMethod.PUT)
    public Result savePositive(@PathVariable(name = "id") String uid, @RequestBody EmployeePositive positive) throws Exception {
        positiveService.save(positive);
        System.out.println("转正："+positive);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 转正表单读取
     */
    @RequestMapping(value = "/{id}/positive", method = RequestMethod.GET)
    public Result findPositive(@PathVariable(name = "id") String uid) throws Exception {
        EmployeePositive positive = positiveService.findById(uid);
        if(positive == null) {
            positive = new EmployeePositive();
            positive.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS,positive);
    }

    /**
     * 历史归档详情列表
     */
    @RequestMapping(value = "/archives/{month}", method = RequestMethod.GET)
    public Result archives(@PathVariable(name = "month") String month, @RequestParam(name = "type") Integer type) throws Exception {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 归档更新
     */
    @RequestMapping(value = "/archives/{month}", method = RequestMethod.PUT)
    public Result saveArchives(@PathVariable(name = "month") String month) throws Exception {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 历史归档列表
     */
    @RequestMapping(value = "/archives", method = RequestMethod.GET)
    public Result findArchives(@RequestParam(name = "pagesize") Integer pagesize, @RequestParam(name = "page") Integer page, @RequestParam(name = "year") String year) throws Exception {
        Map map = new HashMap();
        map.put("year",year);
        map.put("companyId",companyId);
        Page<EmployeeArchive> searchPage = archiveService.findSearch(map, page, pagesize);
        PageResult<EmployeeArchive> pr = new PageResult(searchPage.getTotalElements(),searchPage.getContent());
        return new Result(ResultCode.SUCCESS,pr);
    }

    /**
     * 当月人事报表导出，百万数据报表导出【SXSSFWorkbook】
     *  参数：年-月（2018-02%）
     */
   @GetMapping("/export/{month}")
    public void export(@PathVariable String month) throws Exception {
        //1.获取报表数据
        List<EmployeeReportResult>  list=userCompanyPersonalService.findByReport(companyId,month);
        //2.构造Excel
        //创建工作簿,XSSFWorkbook ：2007版本的Excel
        //Workbook wb=new XSSFWorkbook();
       //SXSSFWorkbook：百万数据报表导出对象。
       SXSSFWorkbook wb = new SXSSFWorkbook(100);//rowAccessWindowSize：阈值，内存中的对象数量到达的最大数量
       //构造sheet
        Sheet sheet = wb.createSheet();
        //创建行

        //创建标题
        String[] titles="编号,姓名,手机,最高学历,国家地区,护照号,籍贯,生日,属相,入职时间,离职类型,离职原因,离职时间".split(",");
        //处理标题
        Row row = sheet.createRow(0);//参数（索引）从0开始

        int titleIndex=0;//单元格的索引，从0开始
        //把标题保存到第一行的单元格当中去
        for (String title : titles) {
            Cell cell = row.createCell(titleIndex++);
            cell.setCellValue(title);
        }

        int rowIndex=1;//数据保存从第二行开始，第一行为标题
        Cell cell=null;
        //把从数据库查询出来的值，保存到工作簿当中去
        for (EmployeeReportResult employeeReportResult : list) {
            row = sheet.createRow(rowIndex++);
            //创建单元格
            //编号,
            cell = row.createCell(0);//第二行的第一个单元格
            cell.setCellValue(employeeReportResult.getUserId());
            // 姓名,
            cell = row.createCell(1);//第二行的第一个单元格
            cell.setCellValue(employeeReportResult.getUsername());
            // 手机,
            cell = row.createCell(2);//第二行的第二个单元格
            cell.setCellValue(employeeReportResult.getMobile());
            // 最高学历,
            cell = row.createCell(3);//第二行的第三个单元格
            cell.setCellValue(employeeReportResult.getTheHighestDegreeOfEducation());
            // 国家地区,
            cell = row.createCell(4);//第二行的第四个单元格
            cell.setCellValue(employeeReportResult.getNationalArea());
            // 护照号,
            cell = row.createCell(5);//第二行的第一五个单元格
            cell.setCellValue(employeeReportResult.getPassportNo());
            // 籍贯,
            cell = row.createCell(6);//第二行的第六个单元格
            cell.setCellValue(employeeReportResult.getNativePlace());
            // 生日,
            cell = row.createCell(7);//第二行的第七个单元格
            cell.setCellValue(employeeReportResult.getBirthday());
            // 属相,
            cell = row.createCell(8);//第二行的第八个单元格
            cell.setCellValue(employeeReportResult.getZodiac());
            // 入职时间,
            cell = row.createCell(9);//第二行的第九个单元格
            cell.setCellValue(employeeReportResult.getTimeOfEntry());
            // 离职类型,
            cell = row.createCell(10);//第二行的第十个单元格
            cell.setCellValue(employeeReportResult.getTypeOfTurnover());
            // 离职原因,
            cell = row.createCell(11);//第二行的第十一个单元格
            cell.setCellValue(employeeReportResult.getReasonsForLeaving());
            // 离职时间
            cell = row.createCell(12);//第二行的第十二个单元格
            cell.setCellValue(employeeReportResult.getResignationTime());
        }

        //3.完成下载
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        System.out.println(month);
        new DownloadUtils().download(os,response,month+"人事报表.xlsx");
    }

    /**
     * 采用模板打印的形式完成报表生成
     *  模块
     *  参数：年-月（2018-02%）
     *  sxssf对象不支持模块打印
     * @param month
     * @throws Exception
     */
    /*@GetMapping("/export/{month}")
    public void export(@PathVariable String month) throws Exception {
        //1.获取报表数据,从员工详情实体类查询数据
        List<EmployeeReportResult>  list=userCompanyPersonalService.findByReport(companyId,month);
        //2.加载模块文件,Resource：用来读取resource资源文件
        Resource resource=new ClassPathResource("excel-template/hr-demo.xlsx");
        FileInputStream fis = new FileInputStream(resource.getFile());
        //3.通过工具类下载文件
        //rowIndex,styleIndex:2 ===> 从第三行开始写入数据，和提取样式
        System.out.println("===="+month);
        new ExcelExportUtil(EmployeeReportResult.class,2,2).
                export(response,fis,list,month+"人事报表.xlsx");
       *//* //3.根据fis文件流加载、创建工作簿
        Workbook wb=new XSSFWorkbook(fis);
        //4.读取工作表,读取第一页
        Sheet sheet= wb.getSheetAt(0);
        //5.抽取公共样式
        Row row = sheet.getRow(2);//第三行
        //row.getLastCellNum() 得到最后一行
        CellStyle styles[]=new CellStyle[row.getLastCellNum()];
        for (int i=0;i<row.getLastCellNum();i++){
            //得到每一个单元格，从第一个单元格开始得到
            Cell cell = row.getCell(i);
            //得到单元格的样式，将样式保存到styles数组当中
            styles[i]= cell.getCellStyle();
        }
        //6.构造单元格
        int rowIndex=2;//数据保存从第三行开始，第一行为头显示人事报表，第二行为标题
        Cell cell=null;
        //把从数据库查询出来的值，保存到工作簿当中去
        for (EmployeeReportResult employeeReportResult : list) {
            row = sheet.createRow(rowIndex++);
            //创建单元格
            //编号,
            cell = row.createCell(0);//第二行的第一个单元格
            cell.setCellValue(employeeReportResult.getUserId());
            cell.setCellStyle(styles[0]);//给单元格设置样式
            // 姓名,
            cell = row.createCell(1);//第二行的第一个单元格
            cell.setCellValue(employeeReportResult.getUsername());
            cell.setCellStyle(styles[1]);//给单元格设置样式
            // 手机,
            cell = row.createCell(2);//第二行的第二个单元格
            cell.setCellValue(employeeReportResult.getMobile());
            cell.setCellStyle(styles[2]);//给单元格设置样式
            // 最高学历,
            cell = row.createCell(3);//第二行的第三个单元格
            cell.setCellValue(employeeReportResult.getTheHighestDegreeOfEducation());
            cell.setCellStyle(styles[3]);//给单元格设置样式
            // 国家地区,
            cell = row.createCell(4);//第二行的第四个单元格
            cell.setCellValue(employeeReportResult.getNationalArea());
            cell.setCellStyle(styles[4]);//给单元格设置样式
            // 护照号,
            cell = row.createCell(5);//第二行的第一五个单元格
            cell.setCellValue(employeeReportResult.getPassportNo());
            cell.setCellStyle(styles[5]);//给单元格设置样式
            // 籍贯,
            cell = row.createCell(6);//第二行的第六个单元格
            cell.setCellValue(employeeReportResult.getNativePlace());
            cell.setCellStyle(styles[6]);//给单元格设置样式
            // 生日,
            cell = row.createCell(7);//第二行的第七个单元格
            cell.setCellValue(employeeReportResult.getBirthday());
            cell.setCellStyle(styles[7]);//给单元格设置样式
            // 属相,
            cell = row.createCell(8);//第二行的第八个单元格
            cell.setCellValue(employeeReportResult.getZodiac());
            cell.setCellStyle(styles[8]);//给单元格设置样式
            // 入职时间,
            cell = row.createCell(9);//第二行的第九个单元格
            cell.setCellValue(employeeReportResult.getTimeOfEntry());
            cell.setCellStyle(styles[9]);//给单元格设置样式
            // 离职类型,
            cell = row.createCell(10);//第二行的第十个单元格
            cell.setCellValue(employeeReportResult.getTypeOfTurnover());
            cell.setCellStyle(styles[10]);//给单元格设置样式
            // 离职原因,
            cell = row.createCell(11);//第二行的第十一个单元格
            cell.setCellValue(employeeReportResult.getReasonsForLeaving());
            cell.setCellStyle(styles[11]);//给单元格设置样式
            // 离职时间
            cell = row.createCell(12);//第二行的第十二个单元格
            cell.setCellValue(employeeReportResult.getResignationTime());
            cell.setCellStyle(styles[12]);//给单元格设置样式
        }
        //7.下载
        //3.完成下载
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //把wb工作簿里面的对象写到os里面去
        wb.write(os);
        System.out.println(month);
        System.out.println("list:==="+list.toString());
        new DownloadUtils().download(os,response,month+"人事报表.xlsx");*//*
    }*/

}
