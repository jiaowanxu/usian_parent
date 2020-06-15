package com.usian.controller;

import com.usian.pojo.TbUser;
import com.usian.service.SSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/service/sso")
public class SSOController {
    @Autowired
    private SSOService ssoService;

    /**
     * 对用户的注册信息（用户名与电话号码）做数据校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @RequestMapping("/checkUserInfo/{checkValue}/{checkFlag}")
    public boolean checkUserInfo(@PathVariable String checkValue, @PathVariable Integer checkFlag){
        return ssoService.checkUserInfo(checkValue,checkFlag);
    }

    /**
     * 用户注册
     * @param tbUser
     * @return
     */
    @RequestMapping("/userRegister")
    public Integer userRegister(@RequestBody TbUser tbUser){
        return ssoService.userRegister(tbUser);
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/userLogin")
    public Map userLogin(String username, String password){
        return ssoService.userLogin(username,password);
    }

    /**
     * 查询用户登录是否过期
     * @param token
     * @return
     */
    @RequestMapping("/getUserByToken/{token}")
    @ResponseBody
    public TbUser getUserByToken(@PathVariable String token){
        return ssoService.getUserByToken(token);
    }

    /**
     * 用户退出登录
     * @param token
     * @return
     */
    @RequestMapping("/logOut")
    public Boolean logOut(String token){
        return ssoService.logOut(token);
    }
}
