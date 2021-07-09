package com.ihrm.common.service;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;

public class BaseService<T> {
    protected Specification<T> getSpec(String companyId){
        Specification<T> spect = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder cb) {
                //根据企业id查询
                //root.get("companyId").as(String.class) ：构造属性，  companyId ：属性值
                //构造查询条件，如： company_id=companyId
                return cb.equal(root.get("companyId").as(String.class),companyId);
            }
        };
        return spect;
    }
}
