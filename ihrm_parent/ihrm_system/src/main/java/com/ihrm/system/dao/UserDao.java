package com.ihrm.system.dao;

import com.ihrm.domain.system.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

public interface UserDao extends JpaRepository<User,String>, JpaSpecificationExecutor<User> {
    public User findByMobile(String mobile);

    @Query(nativeQuery = true,
            value = "SELECT count(*) userTotal ,COUNT(CASE WHEN in_service_status=1 THEN 1 END) userIn, " +
                    "                      COUNT(CASE WHEN in_service_status=2 THEN 1 END) userOut" +
                    "                     FROM bs_user where company_id='1'")
    Map findUserStatus();
}
