package com.hww.orders.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hww.orders.config.mq.RabbitOrderConfig;
import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class OrderSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param msg 订单内容，是json字符串
     */
    @Transactional
    public void send(String msg) {
        System.out.println(">>>>>>订单微服务：向MQ发送消息>>>>>>" + msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setExpiration(1000 * 60 * 30 + ""); // 设置过期时间，单位：毫秒
        // 构建回调返回的数据（消息id）
//        this.rabbitTemplate.setMandatory(true);
//        this.rabbitTemplate.setConfirmCallback(this);
        // 封装消息
        byte[] bytes = msg.getBytes();
        Message message = new Message(bytes, messageProperties);
        //推送消息
        rabbitTemplate.convertAndSend(RabbitOrderConfig.NORMAL_EXCHANGE,
                RabbitOrderConfig.NORMAL_ROUTINGKEY, message);

        log.info("生产者发送消息,Exchange={}", "routingkey={}", RabbitOrderConfig.NORMAL_EXCHANGE,
                RabbitOrderConfig.NORMAL_ROUTINGKEY);
    }

}
