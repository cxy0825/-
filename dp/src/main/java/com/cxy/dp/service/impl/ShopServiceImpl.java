package com.cxy.dp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxy.dp.dto.Result;
import com.cxy.dp.entity.Shop;
import com.cxy.dp.mapper.ShopMapper;
import com.cxy.dp.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Random;
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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    RedisTemplate redisTemplate;

    @Override
    public Result queryById(Long id) {
        //防止缓存穿透
//        Shop shopList = this.queryWithPassThrough(id);
        //用互斥锁防止缓存击穿
        Shop shopList = this.queryWithMutex(id);
        return Result.ok(shopList);

    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        //判断id是不是空
        if (null == id) {
            return Result.fail("店铺id不能为空");
        }

        //更新数据库
        this.updateById(shop);
        //删除redis
        redisTemplate.delete("shop::" + shop.getId());
        return Result.ok("更新成功");
    }

    //防止缓存穿透查询
    public Shop queryWithPassThrough(Long id) {
        String key = "cache:shop:" + id;
        //从redis中获取数据
        Object shop = redisTemplate.opsForValue().get(key);
//        System.out.println(StringUtils.isEmpty(json_shop));
        //查到了数据
        if (!StringUtils.isEmpty(shop)) {
            return (Shop) shop;
        }
        //从数据库中查找
        Shop shopList = getById(id);
        if (shopList == null) {
            //写入redis
            redisTemplate.opsForValue().set(key, "", 60 + new Random().nextInt(50), TimeUnit.MINUTES);
            return null;
        }
        //写入redis
        redisTemplate.opsForValue().set(key, shopList, 60 + new Random().nextInt(50), TimeUnit.MINUTES);
        return shopList;
    }

    //互斥锁 来防止redis缓存击穿
    //获取锁
    public boolean tryLock(String key) {
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, "1", 10L, TimeUnit.SECONDS);
        //使用hutool工具类 来防止自动拆箱过程中Boolean会产生空指针异常
        return BooleanUtil.isTrue(aBoolean);
    }

    //释放锁
    public void unLock(String key) {
        redisTemplate.delete(key);
    }

    //用互斥锁来防止缓存击穿
    public Shop queryWithMutex(Long id) {
        String key = "cache:shop:" + id;
        //从redis中获取数据
        Object shop = redisTemplate.opsForValue().get(key);
//        System.out.println(StringUtils.isEmpty(json_shop));
        //查到了数据
        if (!StringUtils.isEmpty(shop)) {
            return (Shop) shop;
        }
        //从数据库中查找
        String LockKey = "lock:stop:" + id;
        Shop shopList;
        try {
            //先判断有没有拿到互斥锁
            if (!this.tryLock(LockKey)) {
                //没有拿到
                //1s后继续访问看看是不是能拿到缓存
                Thread.sleep(1000);
                return this.queryWithMutex(id);
            }

            //拿到锁说明这个是缓存过期后第一个访问数据库的
            //从数据库中查找
            shopList = getById(id);
            //这个延迟500ms 是模拟数据库重建数据所需要的时间
            Thread.sleep(500);
            if (shopList == null) {
                //写入redis
                redisTemplate.opsForValue().set(key, "", 60 + new Random().nextInt(50), TimeUnit.MINUTES);
                return null;
            }
            //写入redis
            redisTemplate.opsForValue().set(key, shopList, 60 + new Random().nextInt(50), TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        } finally {
            //最终都要释放锁
            this.unLock(LockKey);
        }
        return shopList;
    }
}

