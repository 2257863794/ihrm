package com.ihrm.system.dao;

import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 权限数据访问接口，
 * */
public interface PermissionDao extends JpaRepository<Permission,String>, JpaSpecificationExecutor<Permission> {
   //根据类型和父id查询权限信息
   List<Permission> findByTypeAndPid(int type,String pid);

   //根据父id查询权限信息
   List<Permission> findByPid(String pid);
}
