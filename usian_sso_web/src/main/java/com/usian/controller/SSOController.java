package com.usian.controller;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户注册与登录
 */
@RestController
@RequestMapping("/frontend/sso")
public class SSOController {
    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    /**
     * 对用户的注册信息（用户名与电话号码）做数据校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @RequestMapping("/checkUserInfo/{checkValue}/{checkFlag}")
    public boolean checkUserInfo(String checkValue,Integer checkFlag){
        return ssoServiceFeign.checkUserInfo(checkValue,checkFlag);
    }

    /**
     * 用户注册
     * @param tbUser
     * @return
     */
    @RequestMapping("/userRegister")
    public Result userRegister(TbUser tbUser){
        Integer userRegister=ssoServiceFeign.userRegister(tbUser);
        if (userRegister==1){
            return Result.ok();
        }
        return Result.error("注册失败");
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/userLogin")
    public Result userLogin(String username,String password){
        Map map=ssoServiceFeign.userLogin(username,password);
        if (map!=null){
            return Result.ok(map);
        }
        return Result.error("登陆失败");
    }

    /**
     * 查询用户登录是否过期
     * @param token
     * @return
     */
    @RequestMapping("/getUserByToken/{token}")
    @ResponseBody
    public Result getUserByToken(@PathVariable String token){
        TbUser tbUser=ssoServiceFeign.getUserByToken(token);
        if (tbUser!=null){
            return Result.ok();
        }
        return Result.error("查无结果");
    }

    /**
     * 用户退出登录
     * @param token
     * @return
     */
    @RequestMapping("/logOut")
    public Result logOut(String token){
        Boolean logOut=ssoServiceFeign.logOut(token);
        if (logOut){
            return Result.ok();
        }
        return Result.error("退出失败");
    }
}
