package com.hww.orders.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hww.common.entity.BsOrderGoods;
import com.hww.common.entity.BsOrders;
import com.hww.orders.mapper.BsOrderGoodsMapper;
import com.hww.orders.mapper.BsOrdersMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class OrderDelReceiver {
    @Autowired
    private BsOrdersMapper bsOrdersMapper;
    @Autowired
    private BsOrderGoodsMapper bsOrderGoodsMapper;



    //接收延迟队列的信息
    @RabbitListener(queues = {"delay-queue"})
    @RabbitHandler
    public void handleMessage(Message msg, Channel channel) throws IOException {
        log.info("死亡消费者接收到了消息,ts={}", LocalDateTime.now().toString());
        Map map = JSON.parseObject(msg.getBody(), Map.class);
        Object bsOrderGoods = map.get("bsOrders");

        BsOrders bsOrders = JSONObject.parseObject(bsOrderGoods.toString(), BsOrders.class);
        log.info("bsOrders={}",bsOrders.getOrderSn());
        QueryWrapper<BsOrderGoods> query = Wrappers.query();
        QueryWrapper<BsOrders> ordersQuery = Wrappers.query();
        ordersQuery.eq("order_sn",bsOrders.getOrderSn());
        query.eq("order_sn",bsOrders.getOrderSn());

        BsOrders bsOrders1 = bsOrdersMapper.selectOne(ordersQuery);
        log.info("bsOrders1={}",bsOrders1);
        if (bsOrders1.getStatus() == 0) {
            bsOrderGoodsMapper.delete(query);
            bsOrdersMapper.delete(ordersQuery);
        }

        //channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
    }
}
