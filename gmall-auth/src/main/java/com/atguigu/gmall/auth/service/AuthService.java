package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties({JwtProperties.class})
public class AuthService {

    @Autowired
    private JwtProperties jwtProperties;

 /*   public AuthService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }*/

    @Autowired
    private GmallUmsClient umsClient;

    public void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //1.远程调用ums的数据接口查询用户信息
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
        //2.判空
        if(userEntity == null){
            throw new UserException("用户名或密码错误!!!");
        }
        //3.生成jwt载荷信息
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userEntity.getId());
        map.put("username",userEntity.getUsername());

        //4.防盗用，加入用户的ip
        String ip = IpUtil.getIpAddressAtService(request);
        map.put("ip",ip);

        try {
            //5. 生成jwt
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            //6. jwt放入cookie
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire() * 60);

            //7. 把用户昵称放入cookie中
            CookieUtils.setCookie(request,response,jwtProperties.getUnick(),userEntity.getNickname(),jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
