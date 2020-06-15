package com.usian.service;

import com.usian.mapper.TbUserMapper;
import com.usian.pojo.TbUser;
import com.usian.pojo.TbUserExample;
import com.usian.redis.RedisClient;
import com.usian.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户登录与注册
 */
@Service
@Transactional
public class SSOServiceImpl implements SSOService {
    @Autowired
    private TbUserMapper tbUserMapper;

    @Autowired
    private RedisClient redisClient;

    @Value("${USER_INFO}")
    private String USER_INFO;

    @Value("${SESSION_EXPIRE}")
    private Long SESSION_EXPIRE;

    /**
     * 对用户的注册信息（用户名与电话号码）做数据校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @Override
    public boolean checkUserInfo(String checkValue, Integer checkFlag) {
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        //1、查询条件根据参数动态生成：1、2分别代表username、phone
        if (checkFlag==1){
            criteria.andUsernameEqualTo(checkValue);
        }else if (checkFlag==2){
            criteria.andPhoneEqualTo(checkValue);
        }
        //2、从tb_user表中查询数据
        List<TbUser> tbUserList = tbUserMapper.selectByExample(tbUserExample);
        //3、判断查询结果，如果没有查询到数据返回true
        if (tbUserList==null || tbUserList.size()==0){
            //4、如果没有就返回true
            return true;
        }
        //5、如果有就返回false
        return false;
    }

    /**
     * 用户注册
     * @param tbUser
     * @return
     */
    @Override
    public Integer userRegister(TbUser tbUser) {
        //将密码进行加密处理
        String password = MD5Utils.digest(tbUser.getPassword());
        tbUser.setPassword(password);
        //补齐数据
        tbUser.setCreated(new Date());
        tbUser.setUpdated(new Date());

        return tbUserMapper.insert(tbUser);
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Map userLogin(String username, String password) {
        //1、密码进行加密
        String pwd = MD5Utils.digest(password);
        //2、判断用户名和密码是否正确
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        criteria.andUsernameEqualTo(username);
        criteria.andPasswordEqualTo(pwd);
        List<TbUser> tbUserList = tbUserMapper.selectByExample(tbUserExample);
        if (tbUserList==null || tbUserList.size()<=0){
            return null;
        }
        //3、登录成功后把tbUser装到redis，并设置失效时间
        TbUser tbUser = tbUserList.get(0);
        //4、登录成功后生成token。token相当于原来的jsessionid，字符串，可以使用uuid
        String token = UUID.randomUUID().toString();
        //5、把用户信息保存到redis中。key就是token，value就是tbUser对象转换成json
        tbUser.setPassword(null);
        redisClient.set(USER_INFO+":"+token,tbUser);
        //设置失效时间
        redisClient.expire(USER_INFO+":"+token,SESSION_EXPIRE);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("token",token);
        hashMap.put("userid",tbUser.getId().toString());
        hashMap.put("username",tbUser.getUsername());
        return hashMap;
    }

    /**
     * 查询用户登录是否过期
     * @param token
     * @return
     */
    @Override
    public TbUser getUserByToken(String token) {
        TbUser tbUser = (TbUser) redisClient.get(USER_INFO + ":" + token);
        //需要重置key的过期时间
        redisClient.expire(USER_INFO + ":" +token,SESSION_EXPIRE);
        return tbUser;
    }

    /**
     * 用户退出登录
     * @param token
     * @return
     */
    @Override
    public Boolean logOut(String token) {
        return redisClient.del(USER_INFO+":"+token);
    }
}
