package com.hww.cloudprovidergas.controller;

import com.hww.cloudprovidergas.service.impl.BsSpecsImpl;
import com.hww.cloudprovidergas.service.impl.CartImpl;
import com.hww.cloudprovidergas.service.impl.MerchandiseSearchImpl;
import com.hww.common.Vo.CartVo;
import com.hww.common.base.Result;
import com.hww.common.entity.BsCart;
import com.hww.common.entity.BsGoods;
import com.hww.common.entity.BsSpecAttrs;
import com.hww.common.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartImpl cartService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MerchandiseSearchImpl MerchandiseSearchService;

    @Autowired
    private BsSpecsImpl bsSpecsService;

    @PostMapping("/add")
    public Result addCart(@RequestBody BsCart cartVo, HttpServletRequest request) {
        String token = request.getHeader("token");
        TokenUtil tokenManager = new TokenUtil();
        String userName = tokenManager.getUserFromToken(token);

        log.info("购物车{}{}", userName,cartVo);
      //  cartService.addCart(cartVo);
        redisTemplate.opsForHash().put(userName + "_cart", cartVo.getSkuId(), cartVo);
        return new Result(true, 1, "成功");
    }

    @GetMapping("/getcount")
    public Result getcount(HttpServletRequest request) {
        String token = request.getHeader("token");
        TokenUtil tokenManager = new TokenUtil();
        String userName = tokenManager.getUserFromToken(token);
        return new Result(true, 1, "成功", redisTemplate.opsForHash().size(userName + "_cart"));
    }

    @PostMapping("/getall")
    public Result getAllCart(HttpServletRequest request) {
        String token = request.getHeader("token");
        TokenUtil tokenManager = new TokenUtil();
        String userName = tokenManager.getUserFromToken(token);
        Map map = redisTemplate.opsForHash().entries(userName + "_cart");
        Iterator<Map.Entry<String, BsCart>> iter = map.entrySet().iterator();
        List<Object> list = new ArrayList<>();
        List<Object> specList = new ArrayList<>();
        while (iter.hasNext()) {
            BsGoods bsGoods = new BsGoods();
            Map.Entry<String, BsCart> entry = iter.next();
            bsGoods.setId(entry.getValue().getSkuId());
            BsGoods goods = MerchandiseSearchService.findByIdGoods(bsGoods);
            goods.setOldPrice(entry.getValue().getPrice());
            //获取到规格的ID
//            String specsAttrsId = entry.getValue().getSpecsAttrsId();
//            List<BsSpecAttrs> specAttrs = bsSpecsService.getSpecAttrs(specsAttrsId);
//            specList.add(specAttrs);
           list.add(goods);
        }
        //redisTemplate.opsForHash().entries(userName+"_cart");
        Map<Object, Object> hashMap = new HashMap<>();
        hashMap.put("goods",list);
        hashMap.put("spec",redisTemplate.opsForHash().entries(userName+"_cart"));
        return new Result(true, 1, "成功", hashMap);
    }
}
