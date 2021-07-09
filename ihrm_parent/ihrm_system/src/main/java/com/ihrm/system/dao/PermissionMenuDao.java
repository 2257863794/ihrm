package com.ihrm.system.dao;

import com.ihrm.domain.system.PermissionApi;
import com.ihrm.domain.system.PermissionMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * 权限菜单数据访问接口，
 * */
public interface PermissionMenuDao extends JpaRepository<PermissionMenu,String>, JpaSpecificationExecutor<PermissionMenu> {
}
