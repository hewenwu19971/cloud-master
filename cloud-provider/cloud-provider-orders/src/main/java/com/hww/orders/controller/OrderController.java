package com.hww.orders.controller;

import com.alibaba.fastjson.JSON;
import com.hww.common.Vo.PayVo;
import com.hww.common.Vo.PrepareVo;
import com.hww.common.base.Result;
import com.hww.common.entity.*;
import com.hww.common.utils.SnowFlakeUtil;
import com.hww.common.utils.TokenUtil;
import com.hww.orders.mq.OrderSender;
import com.hww.orders.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private GoodsOrdersImpl goodsOrdersServic;
    @Autowired
    private AddressesImpl addressesServic;
    @Autowired
    private OrderSender orderSender;
    @Autowired
    private MemmberImpl memmberService;
    @Autowired
    private BsOrderImpl bsOrderService;
    @Autowired
    private TSysUserServiceImpl tSysUserService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BsSpecAttrsServiceImpl bsSpecAttrsService;

    /**
     * {products: [{sku_id: 2699, count: "2"}], address: 109, from: "quick", total_price: 252}
     * 提交订单
     *
     * @param prepareVo
     * @return
     */
    @RequestMapping("/confirm")
    public Result confinrm(@RequestBody PrepareVo prepareVo, HttpServletRequest request) {

        log.info("这是{}", prepareVo);
        Map<Object, Object> map = new HashMap<>();
        //Get User Id
        TokenUtil tokenUtil = new TokenUtil();
        String user = tokenUtil.getUserFromToken(request.getHeader("token"));
        TSysUser userId = tSysUserService.getUserId(user);
        //地址
        BsAddresses address = addressesServic.findAddressById(prepareVo.getAddress());

        //商品
        BsOrders bsOrders = new BsOrders();
        bsOrders.setAddress(address.getAddress());
        bsOrders.setArea(address.getArea());
        bsOrders.setProvince(address.getProvince());
        bsOrders.setCity(address.getCity());
        bsOrders.setMid(Long.valueOf(userId.getId()));
        bsOrders.setConsignee(address.getName());
        bsOrders.setMobile(address.getMobile());
        bsOrders.setOrderSn(prepareVo.getOrderSn());
        map.put("bsOrders", bsOrders);
        String msg = JSON.toJSONString(map);
        goodsOrdersServic.addOrder(bsOrders);
        orderSender.send(msg);

        map.put("payed", 1);
        return new Result(true, 1, "成功", map);
    }

    /**
     * {cartVos: {sku_id: 2699, count: "2"}, address: 0, from: "quick"}
     * 返回 商品信息，和地址
     *
     * @param prepareVo
     * @return
     */
    @RequestMapping("/prepare")
    public Result prepare(@RequestBody PrepareVo prepareVo, HttpServletRequest request) {
        TokenUtil tokenUtil = new TokenUtil();
        String user = tokenUtil.getUserFromToken(request.getHeader("token"));
        log.info(user + "创建订单{}", prepareVo);
        TSysUser userId = tSysUserService.getUserId(user);
        Map<String, Object> map = new HashMap<>();
        List<BsOrderGoods> list = new ArrayList<>();

        for (int i = 0; i < prepareVo.getCartVos().size(); i++) {
            if (prepareVo.getCount() != null) {
                String[] count = prepareVo.getCount().split(",");
                prepareVo.getCartVos().get(i).setCount(Integer.valueOf(count[i]));
            }
            String[] specsAttrsId = prepareVo.getSpecsAttrsId().split(",");
            String[] specid = prepareVo.getSpecsAttrsId().split(",");

            if (specid[i].contains("-")) {
                String[] split = specid[i].replace("-", ",").split(",");
                for (int j = 0; j < split.length; j++) {
                    BsSpecAttrs specPrice = bsSpecAttrsService.getSpecPrice(split[j]);
                    double aDouble = prepareVo.getTotal_price();
                    aDouble += specPrice.getSpecPrice();
                    prepareVo.setTotal_price(aDouble);
                }
            } else {
                BsSpecAttrs specPrice = bsSpecAttrsService.getSpecPrice(specid[i]);
                prepareVo.setTotal_price(specPrice.getSpecPrice());
            }
            long OrderId = SnowFlakeUtil.getSnowflakeId();//id
            log.info("雪花ID{}", OrderId);

            BsOrderGoods bsOrderGoods = new BsOrderGoods();
            BsGoods bsGoods = goodsOrdersServic.findByGoodsId(prepareVo.getCartVos().get(i).getSku_id());
            log.info("/prepare;bsgoods={}", bsGoods);
            bsOrderGoods.setMid(userId.getId());
            bsOrderGoods.setGoodId(bsGoods.getId());//商品id
            bsOrderGoods.setOrderSn(String.valueOf(OrderId));//订单id
            bsOrderGoods.setCateId(String.valueOf(bsGoods.getCateId()));
            bsOrderGoods.setGoodName(bsGoods.getGoodName());
            bsOrderGoods.setGoodDes(bsGoods.getGoodDes());
            bsOrderGoods.setGoodNum(prepareVo.getCartVos().get(i).getCount());//个数
            bsOrderGoods.setGoodSpec(String.valueOf(prepareVo.getCartVos().get(i).getSku_id()));
            bsOrderGoods.setGoodThumb(bsGoods.getGoodThumb());
            String[] count = prepareVo.getCount().split(",");
            bsOrderGoods.setGoodPrice(prepareVo.getTotal_price() * Integer.valueOf(count[i]));

            goodsOrdersServic.addGoodsOrder(bsOrderGoods);
            list.add(bsOrderGoods);

            redisTemplate.opsForHash().put(user + "_order", bsOrderGoods.getOrderSn(), list);
            prepareVo.setTotal_price(0);
        }

        map.put("address", addressesServic.getByAddressesId(userId.getId()));
        map.put("products", list);
        return new Result(true, 1, "成功", map);
    }

    @RequestMapping("/pay")
    public Result pay(@RequestBody PayVo payVo, HttpServletRequest request) {
        TokenUtil tokenUtil = new TokenUtil();
        String user = tokenUtil.getUserFromToken(request.getHeader("token"));
        TSysUser userId = tSysUserService.getUserId(user);
        //BsMembers members = memmberService.findPayPwdByUserId(userId.getId());
        log.info("/pay:{}", payVo);
        if (payVo.getPwd().equals(userId.getPayPwd())) {
            for (int i = 0; i < payVo.getOrderSn().size(); i++) {
                List<BsOrderGoods> list = (List<BsOrderGoods>) redisTemplate.opsForHash().get(user + "_order", payVo.getOrderSn().get(i));
                payVo.setMoney(payVo.getMoney() + list.get(i).getGoodPrice());
            }


            if (userId.getBalance() > payVo.getMoney()) {
                Integer payment = memmberService.payment(payVo, userId, userId.getId());
                if (payment > 0) {
                    bsOrderService.modifyOrderStatus(payVo.getOrderSn());
                    for (int i = 0; i < payVo.getOrderSn().size(); i++) {
                        BsOrderGoods byOrderNo = goodsOrdersServic.findOrderOrdersbByOrderNo(payVo.getOrderSn().get(i));
                        redisTemplate.opsForHash().delete(user + "_cart", byOrderNo.getGoodId());
                    }
                    return new Result(true, 1, "成功");
                }
                return new Result(false, 2, "系统错误，支付失败");
            }
            return new Result(false, 2, "余额不足");
        }
        return new Result(false, 2, "支付密码错误");
    }
}
