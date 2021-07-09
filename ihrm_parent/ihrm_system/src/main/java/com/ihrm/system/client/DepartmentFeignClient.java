package com.ihrm.system.client;

import com.ihrm.common.entity.Result;
import com.ihrm.domain.company.Department;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 声明接口，通过feign调用其他微服务
 */
@FeignClient("ihrm-company")//微服务的名称
public interface DepartmentFeignClient {
    /**
     * 调用公司模块的微服务的接口,根据部门id查询部门的信息
     */
    @GetMapping("company/department/{id}")
    public Result findById(@PathVariable String id );

    /**
     * 根据部门编码和企业id查询部门
     * @param code
     * @param companyId
     * @return
     */
    @PostMapping("company/department/search")
    public Department findByCode(@RequestParam String code, @RequestParam String companyId);
}
