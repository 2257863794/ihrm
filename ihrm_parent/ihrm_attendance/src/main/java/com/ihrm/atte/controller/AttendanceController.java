package com.ihrm.atte.controller;

import com.ihrm.atte.service.ArchiveService;
import com.ihrm.atte.service.AtteService;
import com.ihrm.atte.service.ExcelImportService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.common.util.BeanMapUtils;
import com.ihrm.domain.atte.entity.ArchiveMonthly;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.entity.Attendance;
import com.ihrm.domain.atte.vo.SearchListVo;
import com.netflix.discovery.converters.Auto;
import org.bouncycastle.jce.provider.JCEMac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理的控制层，导出、导入、归档、报表
 */
@RestController
@RequestMapping("/attendances")
public class AttendanceController extends BaseController {
    @Autowired
    private ExcelImportService excelImportService;
    @Autowired
    private AtteService atteService;
    @Autowired
    private ArchiveService archiveService;

    /**
     * 上传考勤数据,导入考勤数据
     */
    @PostMapping("/import")
    public Result importExccel(@RequestParam(name="file") MultipartFile file) throws Exception {
        //new ExcelImportUtil<>()
        excelImportService.importAttendanceExcel(file,companyId);
        return Result.SUCCESS();
    }

    /**
     * 查询考勤数据列表
     */
    @GetMapping
    public Result list(int page,int pagesize) throws Exception {
        Map map=atteService.getAtteDate(companyId,page,pagesize);
        System.out.println(map);
        return new Result(ResultCode.SUCCESS,map);
    }

    /**
     * 根据考勤状态和部门查询数据
     */
    @PostMapping
    public Result item(@RequestBody SearchListVo searchListVo) throws ParseException {
        System.out.println(searchListVo);
        Map<String, Object> map1 = BeanMapUtils.beanToMap(searchListVo);
        System.out.println(map1.get("deptID"));
        System.out.println("map1:=="+map1);
        if (searchListVo.getPage() == null) {
            searchListVo.setPage(1);
        }
        if (searchListVo.getPagesize() == null) {
            searchListVo.setPagesize(10);
        }
        Map map = atteService.findAtte(searchListVo, companyId);
        System.out.println("根据考勤状态和部门查询得到的用户信息map："+map);
        return new Result(ResultCode.SUCCESS,map);
    }

    /**
     * 考勤修改,编辑用户的考勤记录,根据用户的id
     */
    @PutMapping("/{id}")
    public Result editAtte(@RequestBody Attendance attendance){
        atteService.editAtte(attendance);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 获取某月人事报表的数据
     *  attendances/reports?atteDate=201907
     */
    @GetMapping("/reports")
    public Result reports(String atteDate){
        List<ArchiveMonthlyInfo> reports = atteService.getReports(atteDate, companyId);
        return new Result(ResultCode.SUCCESS,reports);
    }

    /**
     * 数据归档，归档某月份报表
     *  参数：归档日期
     *  /attendances/archive/item?archiveDate=201907
     *
     */
    @GetMapping("/archive/item")
    public Result item(String archiveDate){
        //归档service archiveService
        //将数据保存到归档主表和归档明细表
        archiveService.saveArchive(archiveDate,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 新建报表
     * newReports?atteDate=201908&yearMonth=201909
     */
    @GetMapping("/newReports")
    public Result newReports(String yearMonth){
        atteService.newReports(yearMonth,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询员工历史归档列表
     * attendances/reports/year?departmentId=1175310929766055936&year=2021
     */
    @GetMapping("/reports/year")
    public Result findReportsByYear(String year){
        //archiveService 归档service  ArchiveMonthly：归档主表
        List<ArchiveMonthly> list =archiveService.findReportsByYear(year,companyId);
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 查询员工历史归档列表的详情数据，也就是归档明细表里面的数据
     *   参数：id【归档主表的id】
     */
    @GetMapping("/reports/{id}")
    public Result findMonthlyInfoByArcId(@PathVariable String id){
        //通过归档主表的id，主键列，查询数据
        List<ArchiveMonthlyInfo> list =archiveService.findMonthlyInfoByArcId(id);
        return new Result(ResultCode.SUCCESS,list);
    }
    /**
     * 根据用户id和年月查询已归档的考勤明细
     *   /archive/{userId}/{yearMonth}
     */
    @GetMapping("/archive/{userId}/{yearMonth}")
    public Result historyData(@PathVariable String userId,@PathVariable String yearMonth){
        //通过归档主表的id，主键列和考勤日期，查询归档明细表的数据
        ArchiveMonthlyInfo info =archiveService.findUserArchiveDetail(userId,yearMonth);
        return new Result(ResultCode.SUCCESS,info);
    }

}
