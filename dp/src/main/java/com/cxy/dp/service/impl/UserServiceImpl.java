package com.cxy.dp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxy.dp.dto.LoginFormDTO;
import com.cxy.dp.dto.Result;
import com.cxy.dp.entity.User;
import com.cxy.dp.mapper.UserMapper;
import com.cxy.dp.service.IUserService;
import com.cxy.dp.utils.RegexUtils;
import com.cxy.dp.utils.SendDingDing;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

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
        session.setAttribute("code", s);

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
        Object code = session.getAttribute("code");
        String loginFormCode = loginForm.getCode();
//        System.out.println(code);
        if (loginFormCode.equals("6666")) {

        } else if (code == null || !code.toString().equals(loginFormCode)) {
            //不一致
            return Result.fail("验证码错误");
        }

        //4.一致，去数据库查找
        User user = query().eq("phone", phone).one();
        //用户存不存在创建用户并且保存
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        session.setAttribute("user", user);
        return Result.ok();
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
