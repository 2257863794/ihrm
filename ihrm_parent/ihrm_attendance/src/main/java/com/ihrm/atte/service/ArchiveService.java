package com.ihrm.atte.service;

import com.alibaba.fastjson.JSONObject;
import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.util.IdWorker;
import com.ihrm.domain.atte.entity.ArchiveMonthly;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.bo.AtteReportMonthlyBO;
import com.ihrm.domain.atte.vo.ArchiveInfoVO;
import com.ihrm.domain.atte.vo.ArchiveItemVO;
import com.ihrm.domain.atte.vo.ArchiveVO;
import com.ihrm.domain.atte.vo.ReportVO;
import com.ihrm.domain.system.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

//归档
@Service
@Slf4j
public class ArchiveService {

	@Autowired
	private AttendanceDao attendanceDao;

	@Autowired
	private ArchiveMonthlyDao atteArchiveMonthlyDao;

	@Autowired
	private ArchiveMonthlyInfoDao archiveMonthlyInfoDao;


	@Autowired
	private UserDao userDao;

	@Autowired
	private IdWorker idWorkker;

	/**
	 * 数据归档
	 * @param archiveDate
	 */
    public void saveArchive(String archiveDate,String companyId) {
		//1.查询所有企业用户,根据公司id查询所有的用户
		List<User> users= userDao.findByCompanyId(companyId);
		 //创建归档主表对象
		ArchiveMonthly archiveMonthly = new ArchiveMonthly();
		//2.保存归档主表数据
		archiveMonthly.setId(idWorkker.nextId()+"");
		archiveMonthly.setCompanyId(companyId);
		archiveMonthly.setArchiveYear(archiveDate.substring(0,4));//201908截取2019
		archiveMonthly.setArchiveMonth(archiveDate.substring(5));//截取5
		//设置归档主表的总人数
		archiveMonthly.setTotalPeopleNum(users.size());
		//设置全勤人数
		archiveMonthly.setFullAttePeopleNum(users.size());
		//设置是否归档，0归档，1未归档
		archiveMonthly.setIsArchived(0);

		//将归档主表的数据保存到数据库
		atteArchiveMonthlyDao.save(archiveMonthly);

		//3.将归档明细表数据保存到数据库[从表]
		//遍历用户列表，统计每一个用户当月的考勤记录
		for (User user : users) {
			//创建归档明细对象
			ArchiveMonthlyInfo info = new ArchiveMonthlyInfo(user);
			//统计每个用户的考勤记录状况，根据用户的id和归档日期【得到的是年月，根据月份查询出考勤表里面当月用户的所有考勤情况】，查询当月考勤表里面的数据
			//获取考勤状况，统计用户旷工、迟到、正常等等的次数
			Map map=attendanceDao.statisByUser(user.getId(),archiveDate+"%");
			//统计用户旷工、迟到、正常等等的次数到归档明细表里面，也就将用户的旷工、迟到、正常等等的次数保存起来
			info.setStatisData(map);
			info.setId(idWorkker.nextId()+"");//设置明细表的id
			info.setAtteArchiveMonthlyId(archiveMonthly.getId());//设置外键，归档主表的id值

			//将归档明细表对象保存到数据库当中
			archiveMonthlyInfoDao.save(info);
		}

    }

	/**
	 * 根据年份，查询当年的所有考勤历史数据，查询归档主表里面的数据
	 * @param year
	 * @return
	 */
	public List<ArchiveMonthly> findReportsByYear(String year,String companyId) {
		//通过公司id和归档年份，去查询归档主表的数据
		return atteArchiveMonthlyDao.findByCompanyIdAndArchiveYear(companyId, year);
	}

	//根据归档主表的id,去查询归档明细表里面的数据
	public List<ArchiveMonthlyInfo> findMonthlyInfoByArcId(String id) {
		return archiveMonthlyInfoDao.findByAtteArchiveMonthlyId(id);
	}

	//根据用户id和考勤日期【年月】查询归档明细里面的数据，主要是为了显示页面【选择年月】
	public ArchiveMonthlyInfo findUserArchiveDetail(String userId, String yearMonth) {
		return archiveMonthlyInfoDao.findByUserIdAndArchiveDate(userId,yearMonth);
	}
}
