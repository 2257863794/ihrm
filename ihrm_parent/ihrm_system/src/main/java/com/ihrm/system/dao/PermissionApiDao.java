package com.ihrm.system.dao;

import com.ihrm.domain.system.PermissionApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * 权限api接口数据访问接口，
 * */
public interface PermissionApiDao extends JpaRepository<PermissionApi,String>, JpaSpecificationExecutor<PermissionApi> {
}
