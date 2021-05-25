package com.hww.orders.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hww.common.Vo.PrepareVo;
import com.hww.common.entity.BsGoods;
import com.hww.common.entity.BsOrderGoods;
import com.hww.common.entity.BsOrders;
import com.hww.orders.mapper.BsOrderGoodsMapper;
import com.hww.orders.mapper.BsOrdersMapper;
import com.hww.orders.mapper.GoodsMapper;
import com.hww.orders.service.GoodsOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsOrdersImpl implements GoodsOrdersService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private BsOrderGoodsMapper bsOrderGoodsMapper;
    @Autowired
    private BsOrdersMapper bsOrdersMapper;
    @Override
    public BsGoods findByGoodsId(int id) {
        return goodsMapper.selectById(id);
    }

    @Override
    public Integer addGoodsOrder(BsOrderGoods bsOrderGoods) {
        return bsOrderGoodsMapper.insert(bsOrderGoods);
    }

    @Override
    public Integer addOrder(BsOrders bsOrders) {
        return bsOrdersMapper.insert(bsOrders);
    }

    @Override
    public BsOrderGoods findOrderOrdersbByOrderNo(String toString) {
        QueryWrapper<BsOrderGoods> query = Wrappers.query();
        query.eq("order_sn",toString);

        return bsOrderGoodsMapper.selectOne(query);
    }
}
