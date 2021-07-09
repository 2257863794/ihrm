package com.ihrm.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类处理
 * {
 *        "success":"成功",
 *        "code":10000,
 *        "message":"ok",
 *        "data":{
 *            total://总条数
 *            rows://数据列表
 *        }
 *     }
 * @param <T>
 * @author jr
 * @version 2021.05.16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> rows;
}
