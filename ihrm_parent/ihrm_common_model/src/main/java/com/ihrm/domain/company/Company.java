package com.ihrm.domain.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 实体类代码：
 *  属性、构造方法、getter、setter方法
 *
 *  lombok插件：使用注解的形式替换getter setter 构造方法
 *      如何使用插件：
 *          1.安装插件（在工程里面引入相应的依赖即可）
 *          2.使用注解配置
 *              配置实体类上面：
 *               @setter :setter方法
 *               @getter:getter方法
 *               @NoArgsConstructor：无参构造方法
 *               @AllArgsConstructor：全参构造方法
 *               @Data：setter,getter,构造方法
 *  使用jpa操作数据：
 *      配置实体类和数据表的映射关系：jpa注解
 *       1.实体类和表的映射关系。
 *       2.字段和属性的映射关系。
 *          1.主键属性的映射。
 *          2.普通属性的映射
 *
 */

@Entity
@Table(name = "co_company")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company implements Serializable {
    private static final long serialVersionUID = 594829320797158219L;
    //ID
    @Id
    private String id;
    /**
     * 公司名称
     */
    private String name;
    /**
     * 企业登录账号ID
     */
    private String managerId;
    /**
     * 当前版本
     */
    private String version;
    /**
     * 续期时间
     */
    private Date renewalDate;
    /**
     * 到期时间
     */
    private Date expirationDate;
    /**
     * 公司地区
     */
    private String companyArea;
    /**
     * 公司地址
     */
    private String companyAddress;
    /**
     * 营业执照-图片ID
     */
    private String businessLicenseId;
    /**
     * 法人代表
     */
    private String legalRepresentative;
    /**
     * 公司电话
     */
    private String companyPhone;
    /**
     * 邮箱
     */
    private String mailbox;
    /**
     * 公司规模
     */
    private String companySize;
    /**
     * 所属行业
     */
    private String industry;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 审核状态
     */
    private String auditState;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 当前余额
     */
    private Double balance;
    /**
     * 创建时间
     */
    private Date createTime;
}
