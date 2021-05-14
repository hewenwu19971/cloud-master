package com.hww.orders.controller;

import com.hww.common.base.Result;
import com.hww.common.entity.BsOrders;
import com.hww.orders.service.impl.BsOrderImpl;
import com.hww.orders.service.impl.GoodsOrdersImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberController {
    @Autowired
    private BsOrderImpl bsOrderService;

    @Autowired
    private GoodsOrdersImpl goodsOrdersServic;

    @RequestMapping("/order/view")
    public Result orderDetail(@RequestBody BsOrders bsOrders) {
        log.info("BsOrders:{}", bsOrders);
        List<Object> list = new ArrayList<>();
        list.add(goodsOrdersServic.findOrderOrdersbByOrderNo(bsOrders.getOrderSn().toString()));
        return new Result(true, 1, "成功",list );
    }
}
