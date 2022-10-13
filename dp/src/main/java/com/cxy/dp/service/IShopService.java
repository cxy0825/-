package com.cxy.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cxy.dp.dto.Result;
import com.cxy.dp.entity.Shop;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);
}
