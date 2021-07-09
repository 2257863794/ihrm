package com.ihrm.employee.service;

import com.ihrm.domain.employee.UserCompanyPersonal;
import com.ihrm.domain.employee.response.EmployeeReportResult;
import com.ihrm.domain.system.User;
import com.ihrm.employee.dao.UserCompanyPersonalDao;
import com.ihrm.employee.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created with IDEA
 * Author:xzengsf
 * Date:2018/10/19 9:52
 * Description:
 */
@Service
public class UserCompanyPersonalService {
    @Autowired
    private UserCompanyPersonalDao userCompanyPersonalDao;
    @Autowired
    private UserDao userDao;

    public User findStaffPhotoById(String id){
        return userDao.findById(id).get();
    }

    public void save(UserCompanyPersonal personalInfo) {
        userCompanyPersonalDao.save(personalInfo);
    }

    public UserCompanyPersonal findById(String userId) {
        return userCompanyPersonalDao.findByUserId(userId);
    }

    public List<EmployeeReportResult> findByReport(String companyId,String month){
        return userCompanyPersonalDao.finByReport(companyId,month+"%");
    }

}
