package com.ihrm.domain.atte.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchListVo implements Serializable {
    private static final long serialVersionUID = -8247920133289436000L;
    /**
     * 页码
     */
    private Integer page;
    /**
     * 页尺寸
     */
    private Integer pagesize;
    /**
     * 关键词
     */
    private String keyword;
    /**
     * 选中部门列表
     */
    private List<String> deptID;
    /**
     * 选中的考勤状态
     */
    private String stateID;
}
