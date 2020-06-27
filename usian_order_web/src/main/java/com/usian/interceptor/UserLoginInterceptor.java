package com.usian.interceptor;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在结算之前判断用户是否登录
 */
@Component
public class UserLoginInterceptor implements HandlerInterceptor {
    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    //方法执行前调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //对用户的token做判断
        String token = request.getParameter("token");
        if (StringUtils.isBlank(token)) {
            return false;//不放行
        }
        //如果用户token不为空，则校验用户在redis中是否失效
        TbUser tbUser = ssoServiceFeign.getUserByToken(token);
        if (tbUser == null) {
            return false;
        }
        return true;//放行
    }
}