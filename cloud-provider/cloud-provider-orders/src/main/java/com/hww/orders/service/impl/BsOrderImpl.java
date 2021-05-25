package com.hww.orders.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hww.common.entity.BsOrders;
import com.hww.orders.mapper.BsOrdersMapper;
import com.hww.orders.service.BsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils.eq;

@Service
@Transactional
public class BsOrderImpl implements BsOrderService {
    @Autowired
    private BsOrdersMapper bsOrdersMapper;

    @Override
    public BsOrders findOrderOrdersbByOrderNo(String orderNo) {
        QueryWrapper<BsOrders> query = Wrappers.query();
        query.eq("order_sn", orderNo);
        return bsOrdersMapper.selectOne(query);
    }

    @Override
    public void modifyOrderStatus(List<String> orderSn) {
        BsOrders bsOrders = new BsOrders();
        bsOrders.setStatus(2);

        QueryWrapper<BsOrders> query = Wrappers.query();
        for (String orderId:orderSn) {
            query.eq("order_sn", orderId);
            bsOrdersMapper.update(bsOrders, query);
        }


    }
}
