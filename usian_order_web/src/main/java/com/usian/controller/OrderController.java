package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.OrderServiceFeign;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderShipping;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单服务  controller
 */
@RestController
@RequestMapping("/frontend/order")
public class OrderController {
    @Autowired
    private CartServiceFeign cartServiceFeign;

    @Autowired
    private OrderServiceFeign orderServiceFeign;

    /**
     * 展示订单确认页面
     * @param ids
     * @param userId
     * @return
     */
    @RequestMapping("/goSettlement")
    public Result goSettlement(String[] ids,String userId){
        //获取购物车
        Map<String, TbItem> cart = cartServiceFeign.selectCartByUserId(userId);
        //从购物车中获取选中的商品
        List<TbItem> tbItemList = new ArrayList<>();
        for (String id:ids){
            tbItemList.add(cart.get(id));
        }
        if (tbItemList.size()>0){
            return Result.ok(tbItemList);
        }
        return Result.error("差无结果");
    }

    /**
     * 创建订单
     * @param orderItem
     * @param tbOrder
     * @param tbOrderShipping
     * @return
     */
    @RequestMapping("/insertOrder")
    public Result insertOrder(String orderItem, TbOrder tbOrder, TbOrderShipping tbOrderShipping){
        /**
         * @RequestBody:获取请求体中的json串----->pojo
         * 下游服务controller：
         *      public Result insertOrder(String orderItem, @RequestBody TbOrder tbOrder, @RequestBody TbOrderShipping tbOrderShipping)
         *
         *   因为一个request中只包含了一个requestbody。
         *   所以feign不支持多个@RequestBody
         */
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderItem(orderItem);
        orderInfo.setTbOrder(tbOrder);
        orderInfo.setTbOrderShipping(tbOrderShipping);
        String orderId=orderServiceFeign.insertOrder(orderInfo);
        if (orderId!=null){
            return Result.ok(orderId);
        }
        return Result.error("error");
    }
}
