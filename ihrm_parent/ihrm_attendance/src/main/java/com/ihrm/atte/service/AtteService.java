package com.ihrm.atte.service;

import com.alibaba.fastjson.JSONObject;
import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.util.BeanMapUtils;
import com.ihrm.common.util.DateUtil;
import com.ihrm.common.util.IdWorker;
import com.ihrm.domain.atte.bo.*;
import com.ihrm.domain.atte.entity.*;
import com.ihrm.domain.atte.enums.DeductionEnum;
import com.ihrm.domain.atte.vo.AttePageVO;
import com.ihrm.domain.atte.vo.AtteSalaryStatisticsVO;
import com.ihrm.domain.atte.vo.SearchListVo;
import com.ihrm.domain.employee.EmployeeArchive;
import com.ihrm.domain.system.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.naming.directory.AttributeInUseException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AtteService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private AttendanceDao attendanceDao;

    @Autowired
    private DeductionDictDao deductionDictDao;

    @Autowired
    private UserDao userDao;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private CompanySettingsDao companySettingsDao;

    /**
     * 查询考勤数据，进行分页
     * 1，考勤月
     * 2.分页
     * 3.查询
     *
     * @param companyId
     * @param page
     * @param pagesize
     * @returnff
     */
    public Map getAtteDate(String companyId, int page, int pagesize) throws ParseException {
        //companySettingsDao记录当前设置考勤到那个月份了，进行月份的一个轮转。
        //根据公司id获取考勤月份记录表的数据，
        CompanySettings settings = companySettingsDao.findById(companyId).get();
        //1、得到考勤记录到的月份
        String dataMonth = settings.getDataMonth();
        //2、分页查询用户
        Page<User> page1 = userDao.findPage(companyId, PageRequest.of(page - 1, pagesize));
        //构造响应的对象集合，保存数据
        List<AtteItemBO> list = new ArrayList<>();
        //3.循环所有的用户，获取每一个用户每天的考勤情况
        for (User user : page1.getContent()) {
            //创建响应的对象
            AtteItemBO bo = new AtteItemBO();
            //保存用户的信息到响应对象中
            BeanUtils.copyProperties(user, bo);
            //考勤对象集合
            List<Attendance> attendances = new ArrayList<>();
            //获取当前考勤记录月所有的天数，days的值是年月日，dataMonth是年月
            String[] days = DateUtil.getDaysByYearMonth(dataMonth);//传递20190201
            //循环每天,查询员工、用户的是否考勤了
            for (String day : days) {
                //根据用户id和考勤日期去考勤表里面查询用户的考勤数据
                Attendance attendance = attendanceDao.findByUserIdAndDay(user.getId(), day);
                //如果为空，说明用户这天没有打卡则设置为旷工
                if (attendance == null) {
                    attendance = new Attendance();
                    //如果当前没有考勤记录，则是指考勤状态为旷工，2==旷工
                    attendance.setAdtStatu(2);
                    //设置用户的id
                    attendance.setId(user.getId());
                    //设置考勤的日期
                    attendance.setDay(day);
                }
                //将当前的考勤对象保存到考勤对象集合中
                attendances.add(attendance);
            }
            //将一轮循环过后的考勤对象集合，数据保存响应前端的AtteItemBO对象中的考勤记录字段中
            bo.setAttendanceRecord(attendances);
            //将AtteItemBO对象，保存到AtteItemBO对象集合中
            list.add(bo);
        }
        //封装map，
        Map map = new HashMap<>();
        // 1、数据分页对象(总条数，数据列表)
        PageResult pr = new PageResult<>(page1.getTotalElements(), list);
        //往map中添加分页对象
        map.put("data", pr);
        //2.待处理的考勤数量
        map.put("tobeTaskCount", 0);
        //3.当前考勤的月份，根据考勤-公司设置里面的月份来进行判断的
        int i = Integer.parseInt(dataMonth.substring(4));
        map.put("monthOfReport", i);
        return map;
    }

    //编辑考勤
    public void editAtte(Attendance attendance) {
        //1.查询考勤是否存在，更新
        Attendance vo = attendanceDao.findByUserIdAndDay(attendance.getUserId(), attendance.getDay());
        //2.如果不存在，设置对象id，进行保存操作
        if (vo == null) {
            attendance.setId(idWorker.nextId() + "");
        } else {
            //如果存在，进行更新操作
            //保存考勤id
            attendance.setId(vo.getId());
        }
        attendanceDao.save(attendance);
    }

    /**
     * 根据考勤日期【年月】和公司id,去查询归档明细表里面的数据，获取某月人事报表的数据
     * ArchiveMonthlyInfo：归档明细表
     */
    public List<ArchiveMonthlyInfo> getReports(String atteDate, String companyId) {
        //1.查询所有企业用户,根据公司id查询所有的用户
        List<User> users = userDao.findByCompanyId(companyId);
        //创建归档明细对象的集合
        List<ArchiveMonthlyInfo> list = new ArrayList<>();
        //2.遍历用户列表，统计每一个用户当月的考勤记录
        for (User user : users) {
            //创建归档明细对象
            ArchiveMonthlyInfo info = new ArchiveMonthlyInfo(user);
            //统计每个用户的考勤记录，根据用户的id和考勤日期，查询考勤表里面的数据
            Map map = attendanceDao.statisByUser(user.getId(), atteDate + "%");
            //将用户的考勤记录保存到setStatisData里面，保存用户正常上班或者迟到..的次数
            info.setStatisData(map);
            //将归档明细对象保存到归档明细对象集合中
            list.add(info);
        }
        return list;
    }

    /**
     * 新建报表，将companySetting【atte_company_settings】中的data_month修改为指定的数据【前端传递过来的年月】
     *
     * @param yearMonth
     * @param companyId
     */
    public void newReports(String yearMonth, String companyId) {
        //根据公司id,查询考勤公司设置表里面的数据
        CompanySettings settings = companySettingsDao.findById(companyId).get();
        settings.setDataMonth(yearMonth);
        companySettingsDao.save(settings);

    }

    public Map findAtte(SearchListVo searchListVo, String companyId) throws ParseException {
        System.out.println("公司id："+companyId);
        //companySettingsDao记录当前设置考勤到那个月份了，进行月份的一个轮转。
        //根据公司id获取考勤月份记录表的数据，
        CompanySettings settings = companySettingsDao.findById(companyId).get();
        //1、得到考勤记录到的月份
        String dataMonth = settings.getDataMonth();
        //2、分页查询用户
        Map<String, Object> map1 = BeanMapUtils.beanToMap(searchListVo);
        Page<User> page1=page1 = page1 = userDao.findAll(createSpecification(map1), PageRequest.of(searchListVo.getPage() - 1, searchListVo.getPagesize()));
        System.out.println("根据部门id查询员工信息：" + page1.toString());
        //构造响应的对象集合，保存数据
        List<AtteItemBO> list = new ArrayList<>();
        //3.循环所有的用户，获取每一个用户每天的考勤情况
        for (User user : page1.getContent()) {
            //创建响应的对象
            AtteItemBO bo = new AtteItemBO();
            //判断考勤状态是否与传递过来的一致
            //考勤状态一致，则保存用户的信息到响应对象中
            BeanUtils.copyProperties(user, bo);

            //考勤对象集合
            List<Attendance> attendances = new ArrayList<>();
            //获取当前考勤记录月所有的天数，days的值是年月日，dataMonth是年月
            String[] days = DateUtil.getDaysByYearMonth(dataMonth);//传递20190201
            //循环每天,查询员工、用户的是否考勤了
            for (String day : days) {
                //根据用户id和考勤日期去考勤表里面查询用户的考勤数据
                Attendance attendance = attendanceDao.findByUserIdAndDay(user.getId(), day);
                //如果为空，说明用户这天没有打卡则设置为旷工
                if (attendance == null) {
                    attendance = new Attendance();
                    //如果当前没有考勤记录，则是指考勤状态为旷工，2==旷工
                    attendance.setAdtStatu(2);
                    //设置用户的id
                    attendance.setId(user.getId());
                    //设置考勤的日期
                    attendance.setDay(day);
                }
                //将当前的考勤对象保存到考勤对象集合中
                attendances.add(attendance);
               /* if(attendance !=null){
                    System.out.println(attendance.getAdtStatu().toString()+"====="+searchListVo.getStateID());
                    System.out.println("".equals(searchListVo.getStateID()));
                    if(attendance.getAdtStatu().toString().equals(searchListVo.getStateID())){
                        //考勤状态一致，则保存用户的信息到响应对象中
                        System.out.println("旷工员工=====");
                        //将当前的考勤对象保存到考勤对象集合中
                        attendances.add(attendance);
                    }else if(searchListVo.getStateID().equals("")){
                        //如果为空，说明用户这天没有打卡则设置为旷工
                        if (attendance == null) {
                            attendance = new Attendance();
                            //如果当前没有考勤记录，则是指考勤状态为旷工，2==旷工
                            attendance.setAdtStatu(2);
                            //设置用户的id
                            attendance.setId(user.getId());
                            //设置考勤的日期
                            attendance.setDay(day);
                        }
                        //将当前的考勤对象保存到考勤对象集合中
                        attendances.add(attendance);
                        System.out.println("空串");
                    }
                }*/
            }

            //将一轮循环过后的考勤对象集合，数据保存响应前端的AtteItemBO对象中的考勤记录字段中
            bo.setAttendanceRecord(attendances);
            //将AtteItemBO对象，保存到AtteItemBO对象集合中
            list.add(bo);
        }
        //封装map，
        Map map = new HashMap<>();
        // 1、数据分页对象(总条数，数据列表)
        PageResult pr = new PageResult<>(page1.getTotalElements(), list);
        //往map中添加分页对象
        map.put("data", pr);
        //2.待处理的考勤数量
        map.put("tobeTaskCount", 0);
        //3.当前考勤的月份，根据考勤-公司设置里面的月份来进行判断的
        int i = Integer.parseInt(dataMonth.substring(4));
        map.put("monthOfReport", i);

        return map;

    }

    /**
     * 动态条件构建
     *
     * @param searchMap
     * @return
     */
    private Specification<User> createSpecification(Map searchMap) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                // 企业id
                /*if (searchMap.get("companyId")!=null && !"".equals(searchMap.get("companyId"))) {
                    predicateList.add(cb.like(root.get("companyId").as(String.class), (String)searchMap.get("companyId")));
                }*/
               List<String> list = (List<String>) searchMap.get("deptID");
                if (list.size()!=0 && list!=null) {
                    predicateList.add(cb.and(root.get("departmentId").in(searchMap.get("deptID"))));
                }
                return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        };
    }
}
