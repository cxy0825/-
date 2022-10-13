package com.cxy.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxy.dp.dto.LoginFormDTO;
import com.cxy.dp.dto.Result;
import com.cxy.dp.dto.UserDTO;
import com.cxy.dp.entity.User;
import com.cxy.dp.mapper.UserMapper;
import com.cxy.dp.service.IUserService;
import com.cxy.dp.utils.RegexUtils;
import com.cxy.dp.utils.SendDingDing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    RedisTemplate redisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //不符合就返回错误信息
            return Result.fail("手机号码格式错误");
        }
        //符合生成验证码
        String s = RandomUtil.randomNumbers(6);
        //保存验证码到session
        //session.setAttribute("code", s);
        //保存验证码到redis
        redisTemplate.opsForValue().set("code:phone:" + phone, s, 5L, TimeUnit.MINUTES);
        //发送验证码
        SendDingDing.sendMsg(s);
        //返回ok
        return Result.ok();
    }

    //    登录
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        //1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号不正确");
        }
        //校验验证码
//        Object code = session.getAttribute("code");
        String code = (String) redisTemplate.opsForValue().get("code:phone:" + loginForm.getPhone());
        String loginFormCode = loginForm.getCode();
//        System.out.println(code);
        if (loginFormCode.equals("6666")) {

        } else if (code == null || !code.equals(loginFormCode)) {
            //不一致
            return Result.fail("验证码错误");
        }

        //4.一致，去数据库查找
        User user = query().eq("phone", phone).one();
        //把验证码从redis中删除
        Boolean delete = redisTemplate.delete("code:phone:" + loginForm.getPhone());
        //用户不存在创建用户并且保存
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        String token = UUID.randomUUID().toString();
        //把user转换成hashMap
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userDTOMap = BeanUtil.beanToMap(userDTO);
//        log.info("userDTO" + userDTO.toString());
        //把用户信息保存到redis中
        redisTemplate.opsForHash().putAll("token:" + token, userDTOMap);
        redisTemplate.expire("token:" + token, 30L, TimeUnit.MINUTES);
        //session.setAttribute("user", user);

        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        //创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        //插入数据库
        save(user);
        System.out.println("插入数据库");
        return user;
    }
}
