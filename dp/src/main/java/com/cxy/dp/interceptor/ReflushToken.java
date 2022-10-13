package com.cxy.dp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.cxy.dp.dto.UserDTO;
import com.cxy.dp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * 拦截一切请求，做刷新token用
 *
 * */
@Slf4j
public class ReflushToken implements HandlerInterceptor {
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求中获取token
        String token = request.getHeader("authorization");

        //去redis中查找
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("token:" + token);

        //如果有就放行
        if (entries.isEmpty()) {
            return true;
        }
        //把hashMap转成userDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(entries, new UserDTO(), false);
        //吧用户信息保存到local中
        UserHolder.saveUser(userDTO);
        //刷新token有效期
        redisTemplate.expire("token:" + token, 30L, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除ThreadLocal
        UserHolder.removeUser();
    }
}
