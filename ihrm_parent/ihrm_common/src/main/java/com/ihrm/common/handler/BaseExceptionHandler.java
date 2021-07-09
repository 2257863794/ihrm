package com.ihrm.common.handler;

import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.sun.net.httpserver.Authenticator;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义的公共异常处理器,拦截器
 *  1.声明异常处理器
 *  2.对异常统一处理
 */
@ControllerAdvice
public class BaseExceptionHandler {
    //异常处理机制
    @ExceptionHandler (Exception.class)
    @ResponseBody//返回json字符串
    public Result error(HttpServletRequest request, HttpServletResponse response,Exception e){
        if(e.getClass() == CommonException.class){
            //强制类型转换,子类转换为父类，向上类型转换
            CommonException ce = (CommonException) e;
            Result result = new Result(ce.getResultCode());
            return result;
        }else{
            Result result = new Result(ResultCode.SERVER_ERROR);
            return result;
        }
    }

    @ExceptionHandler(value = AuthorizationException.class)
    @ResponseBody
    public Result error(HttpServletRequest request, HttpServletResponse response,AuthorizationException e) {
        return new Result(ResultCode.UNAUTHORISE);
    }
}
