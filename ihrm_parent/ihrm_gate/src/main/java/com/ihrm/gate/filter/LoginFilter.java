package com.ihrm.gate.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义过滤器
 */
//@Component
public class LoginFilter extends ZuulFilter {
    /**
     * String类型的返回值
     * 定义过滤器类型的
     *    pre：在执行路由请求之前执行
     *    routingL：在路由请求时调用
     *    post：在routing和error过滤器之后执行
     *    error：处理请求出现异常的时候执行
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * int类型的返回值：
     *      定义过滤器的优先级：数字越小优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 2;
    }

    /**
     * boolean类型的返回值
     *  判断过滤器是否需要执行
     *  false ：不执行,不需要过滤
     *  true：需要过滤
     * @return
     */
    @Override
    public boolean shouldFilter() {
        //对某些请求进行过滤（不执行过滤器）
        //返回true永远执行过滤器
        return true;
    }

    /**
     * run方法：过滤器中赋值的具体业务逻辑
     * 使用过滤器进行jwt鉴权，对是否登录进行验证
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
       // System.out.println("执行了LoginFilter的run方法");
        //1.获取请求对象request
        //1.1获取Zuul提供的请求上下文的对象（工具类）
        /*RequestContext rc=RequestContext.getCurrentContext();
        //1.2 从上下文对象获取request对象
        HttpServletRequest request = rc.getRequest();
        //2.从request中获取Authonrization的头信息
        String token = request.getHeader("Authorization");
        //3.判断
        if(token == null || "".equals(token)){
            //没有传递token信息，没有登录需要进行登录，进行拦截
            rc.setSendZuulResponse(false);//拦截，不发送请求了
            //返回错误的401状态码
            rc.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
        }*/
        return null;//继续往后面执行
    }
}
