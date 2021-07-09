package com.ihrm.atte.service;

import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.util.BeanMapUtils;
import com.ihrm.common.util.IdWorker;
import com.ihrm.domain.atte.entity.*;
import com.ihrm.domain.atte.enums.DeductionEnum;
import com.ihrm.domain.atte.enums.LeaveTypeEnum;
import com.ihrm.domain.atte.vo.ConfigVO;
import com.ihrm.domain.atte.vo.ExtDutyVO;
import com.ihrm.domain.atte.vo.ExtWorkVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;


/**
 * 考勤模块中的设置
 */
@Service
public class ConfigurationService{

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private LeaveConfigDao leaveConfigDao;

    @Autowired
    private DeductionDictDao deductionDictDao;

    @Autowired
    private ExtraDutyConfigDao extraDutyConfigDao;//考勤

    @Autowired
    private ExtraDutyRuleDao extraDutyRuleDao;

    @Autowired
    private DayOffConfigDao dayOffConfigDao;

    @Autowired
    private IdWorker idWorker;

    /**
     * 查询考勤设置
     * @param companyId
     * @param departmentId
     * @return
     */
    public AttendanceConfig getAttenConfig(String companyId, String departmentId) {
        return attendanceConfigDao.findByCompanyIdAndDepartmentId(companyId,departmentId);
    }

    /**
     * 保存或者更新考勤设置
     * @param attendanceConfig
     */
    public void saveAtteConfig(AttendanceConfig attendanceConfig) {
        //1.查询是否存在响应的考勤记录
        AttendanceConfig vo = attendanceConfigDao.findByCompanyIdAndDepartmentId(attendanceConfig.getCompanyId(), attendanceConfig.getDepartmentId());
        //2.如果存在，更新
        if(vo!=null){
            //把从数据库里面得到到考勤表的id保存到attendanceConfig中
            attendanceConfig.setId(vo.getId());
        }else {
            //3.如果不存在，设置id保存，通过idWorker工具类，生成id
            attendanceConfig.setId(idWorker.nextId()+"");
        }
        attendanceConfigDao.save(attendanceConfig);
    }

    /**
     * 请假设置信息的查询
     * @param companyId
     * @param departmentId
     * @return
     */
    public List<LeaveConfig> getLeaveCfg(String companyId, String departmentId) {
        return leaveConfigDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
    }

    /**
     * 保存或者更新请假信息的设置
     */
    public void leaveConfigSaveOrUpdate(LeaveConfig leaveConfig){
        //根据请假的类型，部门id、公司id查询请假表的数据
        LeaveConfig demo = leaveConfigDao.findByCompanyIdAndDepartmentIdAndLeaveType(leaveConfig.getCompanyId(), leaveConfig.getDepartmentId(), leaveConfig.getLeaveType());
        if(demo!=null){
            //不为空做修改操作
            //将请假表的id保存到leaveConfig里面
            leaveConfig.setId(demo.getId());
        }else{
            //为空做保存操作,调用idWorker工具类生成id
            leaveConfig.setId(idWorker.nextId()+"");
        }
        //执行保存或者更新操作
        leaveConfigDao.save(leaveConfig);
    }

    /**
     * 根据部门id和公司id查询扣款设置表里面的信息
     * @param companyId
     * @param departmentId
     * @return
     */
    public List<DeductionDict> getDedCfgList(String companyId, String departmentId) {
        return deductionDictDao.findByCompanyIdAndDepartmentId(companyId,departmentId);
    }

    /**
     * 扣款设置信息的保存或者更新
     * @param deductionDict
     */
    public void deductionSaveOrUpdate(DeductionDict deductionDict) {
        DeductionDict demo = deductionDictDao.findByCompanyIdAndDepartmentIdAndDedTypeCode(deductionDict.getCompanyId(), deductionDict.getDepartmentId(), deductionDict.getDedTypeCode());
        if(demo!=null){
            //demo不为空，则将扣款表的id保存到deductionDict类里面
            deductionDict.setId(demo.getId());
        }else{
            //demo为空，则调用idWorker类生成id
            deductionDict.setId(idWorker.nextId()+"");
        }
        //进行保存或者更新操作
        deductionDictDao.save(deductionDict);
    }

    /**
     * 加班设置信息的查询
     * @param companyId
     * @param departmentId
     * @return
     */
    public Map getExtWorkCfg(String companyId, String departmentId) {
        //封装了加班规则表实体类ExtraDutyRule、调休假设置表实体类DayOffConfig、加班设置表实体类ExtraDutyConfig
        ExtWorkVO extWorkVO = new ExtWorkVO();
        //或者加班规则表里面的信息
        List<ExtraDutyRule> extraDutyRuleList = extraDutyRuleDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
        //获取加班设置表里面的信息
        ExtraDutyConfig extraDutyConfig = extraDutyConfigDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
        //获取调休设置表里面的信息
        DayOffConfig dayOffConfig = dayOffConfigDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
        extWorkVO.setExtraDutyRuleList(extraDutyRuleList);
        extWorkVO.setExtraDutyConfig(extraDutyConfig);
        extWorkVO.setDayOffConfigs(dayOffConfig);

        //将类对象转换为一个map集合
        return BeanMapUtils.beanToMap(extWorkVO);
    }

    /**
     * 加班设置信息的保存和更新
     * @param atteExtDutyVO
     */
    @Transactional(rollbackFor =Exception.class )
    public void extDetySaveOrUpdate(ExtDutyVO atteExtDutyVO) {
        String companyId = atteExtDutyVO.getCompanyId();
        String departmentId = atteExtDutyVO.getDepartmentId();
        //每日标准工作时长，单位小时
        String workHoursDay = atteExtDutyVO.getWorkHoursDay();
        //是否打卡
        Integer isClock = atteExtDutyVO.getIsClock();
        //是否开启加班补偿
        String isCompensationint = atteExtDutyVO.getIsCompensationint();
        //调休最后有效日期
        String latestEffectDate=atteExtDutyVO.getLatestEffectDate();
        System.out.println("调休最后有效日期："+latestEffectDate);
        //调休单位
        String unit = atteExtDutyVO.getUnit();

        //加班设置实体类
        //创建加班设置实体类对象
        ExtraDutyConfig extraDutyConfig = new ExtraDutyConfig();
        //根据公司id和部门id查询数据库得到加班设备实体类对象
        ExtraDutyConfig extraDutyConfigItem  = extraDutyConfigDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
        Date now = new Date();//得到现在的时间
        if(extraDutyConfigItem!=null){
            //不为空进行更新操作
            extraDutyConfigItem.setUpdateDate(now);//设置更新的时间，不是加班设置表里面的字段

            //参数更新
            extraDutyConfigItem.setCompanyId(companyId);
            extraDutyConfigItem.setDepartmentId(departmentId);
            extraDutyConfigItem.setWorkHoursDay(workHoursDay);//每日标准工作时长，单位小时
            extraDutyConfigItem.setIsClock(isClock);//是否打卡
            extraDutyConfigItem.setIsCompensationint(isCompensationint);//是否开启加班补偿
            //进行更新操作
            extraDutyConfigDao.save(extraDutyConfigItem);
        }else{
            //为空，进行保存操作
            //公共参数,BaseEntity实体类里面的属性
            extraDutyConfig.setCreateDate(now);//设置更新的时间，不是加班设置表里面的字段
            //参数赋值
            extraDutyConfig.setId(idWorker.nextId()+"");
            extraDutyConfig.setCompanyId(companyId);
            extraDutyConfig.setDepartmentId(departmentId);
            extraDutyConfig.setWorkHoursDay(workHoursDay);
            extraDutyConfig.setIsClock(isClock);
            extraDutyConfig.setIsCompensationint(isCompensationint);
            //进行保存操作
            extraDutyConfigDao.save(extraDutyConfig);
        }

        //加班规则设置
        //根据公司id和部门id查询数据库得到加班设置实体类对象ExtraDutyConfig
        ExtraDutyConfig cfg=extraDutyConfigDao.findByCompanyIdAndDepartmentId(companyId,departmentId);
        //得到加班设置表的id
        String extraDutyonfigId = cfg.getId();
        //得到加班规则的对象集合
        List<ExtraDutyRule> rules = atteExtDutyVO.getRules();
        // boolean isEmpty()：判断集合中是否包含有元素，没有元素返回true
        //每次公司的加班规则都整体更新
        if(rules!=null && !rules.isEmpty()){
            //rules不为空，则做更新操作，但是每次公司的加班规则都做整体的更新
            //根据加班设置表的id删除，把加班规则表里面的数据删除掉
            extraDutyRuleDao.deleteByExtraDutyConfigId(extraDutyonfigId);
            //重新指定加班规则表里面的数据,因为加班规则有【工作日、休息日、法定节假日可申请加班三种】，每次做更新麻烦，所以删除重新添加数据
            for (ExtraDutyRule extraDutyRule : rules) {//循环得到的加班规则对象的集合
                //公共参数,BaseEntity实体类里面的属性
                extraDutyRule.setCreateDate(now);//设置更新的时间，不是加班设置表里面的字段
                //设置参数
                //设置加班规则表的id
                extraDutyRule.setId(idWorker.nextId()+"");
                //设置加班设置表的id
                extraDutyRule.setExtraDutyConfigId(extraDutyonfigId);
                //设置公司id
                extraDutyRule.setCompanyId(companyId);
            }
            extraDutyRuleDao.saveAll(rules);
        }

        //调休假设置
        //根据公司id和部门id得到调休假的实体类对象
        DayOffConfig dayOffConfigItem = dayOffConfigDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
        //创建调休假的对象
        DayOffConfig dayOffConfig = new DayOffConfig();
        if(dayOffConfigItem !=null){
            //得到的调休假的对象不为空，则进行更新操作
            //公共参数设置,BaseEntity实体类里面的属性
            dayOffConfigItem.setUpdateDate(now);
            //参数设置
            dayOffConfigItem.setCompanyId(companyId);
            dayOffConfigItem.setDepartmentId(departmentId);
            dayOffConfigItem.setUnit(unit);
            dayOffConfigItem.setLatestEffectDate(latestEffectDate);
            //进行更新操作
            dayOffConfigDao.save(dayOffConfigItem);
        }else{
            //为空则进行保存操作
            //设置调休假表的id值
            dayOffConfig.setId(idWorker.nextId()+"");
            dayOffConfig.setUnit(unit);
            dayOffConfig.setLatestEffectDate(latestEffectDate);
            dayOffConfig.setCompanyId(companyId);
            dayOffConfig.setDepartmentId(departmentId);
            dayOffConfigDao.save(dayOffConfig);
        }
    }
}
