package com.ihrm.atte.controller;

import com.ihrm.atte.service.ConfigurationService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.atte.entity.AttendanceConfig;
import com.ihrm.domain.atte.entity.DeductionDict;
import com.ihrm.domain.atte.entity.LeaveConfig;
import com.ihrm.domain.atte.vo.ExtDutyVO;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.security.krb5.Config;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 配置考勤设置的controller
 * 考勤模块中的设置中的设置出勤【设置每个部门上下班时间和查询每个部门上下班时间】
 */
@RestController
@RequestMapping("/cfg")
public class ConfigController extends BaseController {
    @Autowired
    private ConfigurationService configurationService;

    /**
     * 获取考勤设置,根据部门id查询出勤设置表里面的数据
     * cfg/atte/item
     * post
     * 参数：departmentId
     */
    @GetMapping("/atte/item")
    public Result atteConfig(String departmentId) {
        System.out.println(departmentId);
        System.out.println(companyId);
        AttendanceConfig ac = configurationService.getAttenConfig(companyId, departmentId);
        System.out.println("===" + ac.toString());
        return new Result(ResultCode.SUCCESS, ac);
    }

    /**
     * 保存或者修改出勤设置，上下班时间
     * cfg/atte/item
     * post
     * 参数：departmentId
     * AttendanceConfig：出勤表的实体类
     */
    @PutMapping("/atte")
    public Result saveAtteConfig(@RequestBody AttendanceConfig attendanceConfig) {
        attendanceConfig.setCompanyId(companyId);
        configurationService.saveAtteConfig(attendanceConfig);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据部门id，查询请假设置表里面的数据
     * 请假设置信息查询
     */
    @GetMapping("/leave/list")
    public Result leaveCfgItem(String departmentId) {
        System.out.println(departmentId);
        System.out.println(companyId);
        List<LeaveConfig> leaveConfigList = configurationService.getLeaveCfg(companyId, departmentId);
        return new Result(ResultCode.SUCCESS, leaveConfigList);
    }

    /**
     * 请假设置信息的保存和更新
     */
    @PutMapping("/leave")
    public Result leaveSaveOrUpdate(@RequestBody List<LeaveConfig> leaveConfigs) {

        for (LeaveConfig leaveConfig : leaveConfigs) {
            //给每一个LeaveConfig对象设置公司id
            leaveConfig.setCompanyId(companyId);
            //调用configurationService业务层里面的leaveConfigSaveOrUpdate方法做修改或者保存操作
            configurationService.leaveConfigSaveOrUpdate(leaveConfig);

        }
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 扣款设置信息的查询
     */
    @GetMapping("/ded/list")
    public Result dedCfgItem(String departmentId) {
        List<DeductionDict> deductionDictList = configurationService.getDedCfgList(companyId, departmentId);
        return new Result(ResultCode.SUCCESS, deductionDictList);
    }

    /**
     * 扣款设置信息的保存和更新
     */
    @PutMapping("/deduction")
    public Result deductionSaveOrUpdate(@RequestBody List<DeductionDict> dictList) {

        for (DeductionDict deductionDict : dictList) {
            //给每一个DeductionDict对象设置公司id
            deductionDict.setCompanyId(companyId);
            //调用configurationService业务层里面的deductionSaveOrUpdate方法做修改或者保存操作
            configurationService.deductionSaveOrUpdate(deductionDict);

        }
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 加班设置信息的查询
     */
    @GetMapping("/extDuty/item")
    public Result extWorkCfgItem(String departmentId) {
        Map map = configurationService.getExtWorkCfg(companyId, departmentId);
        return new Result(ResultCode.SUCCESS, map);
    }

    /**
     * 加班设置信息的保存和更新
     * ExtDutyVO ：用来接收前端传递过来的数据，包括加班规则表、调休假设置表、加班设置表里面的数据【加班设置功能】
     */
    @PutMapping("/extDuty")
    public Result extDetySaveOrUpdate(@RequestBody @Valid ExtDutyVO atteExtDutyVO) {
        //给每一个ExtDutyVO对象设置公司id
        atteExtDutyVO.setCompanyId(companyId);
        //调用configurationService业务层里面的deductionSaveOrUpdate方法做修改或者保存操作
        configurationService.extDetySaveOrUpdate(atteExtDutyVO);
        return new Result(ResultCode.SUCCESS);
    }


}

