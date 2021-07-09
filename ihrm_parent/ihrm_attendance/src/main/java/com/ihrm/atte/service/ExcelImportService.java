package com.ihrm.atte.service;

import com.ihrm.atte.dao.AttendanceConfigDao;
import com.ihrm.atte.dao.AttendanceDao;
import com.ihrm.atte.dao.UserDao;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.common.util.DateUtil;
import com.ihrm.common.util.IdWorker;
import com.ihrm.domain.atte.entity.Attendance;
import com.ihrm.domain.atte.entity.AttendanceConfig;
import com.ihrm.domain.atte.vo.AtteUploadVo;
import com.ihrm.domain.system.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class ExcelImportService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AttendanceDao attendanceDao;

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private IdWorker idWorker;
    @Value("${atte.workingDays}")
    private String workingDays;

	@Value("${atte.holidays}")
	private String holidays;

//    @Value("attendance.workingDays")
//    private String workingDays;
//
//	@Value("attendance.holidays")
//	private String holidays;

    /**
     * 处理考勤数据的文件上传
     *    参数：excel文件
     *          企业id
     * @param file
     * @param companyId
     */
    public void importAttendanceExcel(MultipartFile file, String companyId) throws Exception {
        //1.需要将导入的excel文件解析为AtteUploadVo的list集合，AtteUploadVo类中的属性对应Excel中的列
        List<AtteUploadVo> list = new ExcelImportUtil<AtteUploadVo>(AtteUploadVo.class).readExcel(file.getInputStream(), 1, 0);
        //2.循环list集合
        for (AtteUploadVo atteUploadVo : list) {
            //2.1根据上传的手机号码查询用户
            User user = userDao.findByMobile(atteUploadVo.getMobile());
            //2.2 构造考勤对象
            Attendance attendance = new Attendance(atteUploadVo,user);
            //设置考勤时间
            attendance.setDay(atteUploadVo.getAtteDate());
           // attendance.setDay(DateUtil.parseDate2String(atteUploadVo.getInTime(),"yyyyMMdd"));
            //2.3 判断是否是休假的考勤记录
            //将国家规定的假期记录到文件中，application.yml文件里面
            if(holidays.contains(atteUploadVo.getAtteDate())){
                //如果是法定节假日，就休息
                //setAdtStatu考勤状态，23：代表休息
                attendance.setAdtStatu(23);//休息
            }else if(DateUtil.isWeekend(atteUploadVo.getAtteDate()) && workingDays.contains(atteUploadVo.getAtteDate())){
                //如果是周六、周天，并且周六或者周天不上班则，设置为休息
                attendance.setAdtStatu(23);//休息
            }else {
                //2.4 判断迟到、早退的状态
                //1.查询当前员工部门的上班时间，查询当前员工部门的下班时间
                //得到出勤设置表数据
                AttendanceConfig ac = attendanceConfigDao.findByCompanyIdAndDepartmentId(companyId, user.getDepartmentId());
                //2.比较上班时间是否晚于规定上班时间（迟到）,getMorningStartTime:规定时间，getInTime：上班时间，打卡时间
                //返回true则上班没有迟到，false 迟到了,！取反判断它迟到的情况【上班规定时间晚于上班打卡时间，返回true，没有迟到】
                //设置考勤状况
                if(!DateUtil.comparingDate(ac.getAfternoonStartTime(),atteUploadVo.getInTime())){
                    attendance.setAdtStatu(3);//3===迟到
                }else if(DateUtil.comparingDate(ac.getAfternoonEndTime(),atteUploadVo.getOutTime())){
                    //3.比较下班时间是否早于规定下班时间（早退）
                    //返回true则下班早退【下班规定时间晚于下班打卡时间,返回true，早退了】
                    attendance.setAdtStatu(4);//4===早退
                }else{
                    attendance.setAdtStatu(1);//1===正常
                }
            }
            //2.5 查询用户是否已经有考勤记录，如果不存在，保存进数据库，防止考勤作弊
            //根据用户id和考勤日期查询考勤是否存在数据
            Attendance byUserIdAndDay = attendanceDao.findByUserIdAndDay(user.getId(), atteUploadVo.getAtteDate());
            if(byUserIdAndDay ==null){
                attendance.setId(idWorker.nextId()+"");
                attendanceDao.save(attendance);
            }
        }

    }
}
