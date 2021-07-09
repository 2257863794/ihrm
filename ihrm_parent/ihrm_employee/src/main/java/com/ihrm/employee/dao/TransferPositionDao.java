package com.ihrm.employee.dao;

import com.ihrm.domain.employee.EmployeeTransferPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * 数据访问接口
 */
public interface TransferPositionDao extends JpaRepository<EmployeeTransferPosition, String>, JpaSpecificationExecutor<EmployeeTransferPosition> {
    EmployeeTransferPosition findByUserId(String uid);
    //根据用户id查询岗位表里面的岗位和调岗表里面的时间和原因,UserCompanyJobs：岗位表，EmployeeTransferPosition：调岗表
    @Query(nativeQuery = true,value =
            "select p.adjustment_time,p.cause_of_adjusting_post,job.post" +
                    " from em_transferposition p left join em_user_company_jobs job on p.user_id=job.user_id where job.user_id=?1")
    Map findPostAndTimeCause(String userId);

}