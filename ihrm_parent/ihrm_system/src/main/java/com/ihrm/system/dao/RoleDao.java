package com.ihrm.system.dao;

import com.ihrm.domain.system.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 角色实体类
 */
public interface RoleDao extends JpaRepository<Role,String>, JpaSpecificationExecutor<Role> {
}
