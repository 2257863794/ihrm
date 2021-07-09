package com.ihrm.domain.atte.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

//记录当前设置考勤到那个月份了，进行月份的一个轮转。
//考勤-企业设置信息
@Entity
@Table(name = "atte_company_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySettings implements Serializable {
    private static final long serialVersionUID = 5753095342370704726L;
    /**
     * 企业id
     */
    @Id()
    @Column(name = "company_id")
    private String companyId;
    /**
     * 是否设置 0为未设置，1为已设置
     */
    private Integer isSettings;
    /**
     * 当前显示报表月份
     */
    private String dataMonth;
}
