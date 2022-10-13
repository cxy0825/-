package com.cxy.dp.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cxy.dp.dto.Result;
import com.cxy.dp.entity.ShopType;
import com.cxy.dp.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
@Slf4j
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;
    @Autowired
    private IShopTypeService iShopTypeService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("list")
    public Result queryTypeList(HttpServletRequest request) {
//        log.info(request.getRequestURI());
        List<ShopType> l = (List) redisTemplate.opsForValue().get("type-list");
        //如果redis中有这个就直接返回
        if (null != l) {
            return Result.ok(l);
        }
        //否则查询数据库 添加到缓存
        LambdaQueryWrapper<ShopType> shopTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shopTypeLambdaQueryWrapper.orderByAsc(ShopType::getSort);
        List<ShopType> list = iShopTypeService.list(shopTypeLambdaQueryWrapper);
        redisTemplate.opsForValue().set("type-list", list, 120, TimeUnit.MINUTES);
        return Result.ok(list);
//        List<ShopType> typeList = typeService
//                .query().orderByAsc("sort").list();
//        return Result.ok(typeList);
    }
}
