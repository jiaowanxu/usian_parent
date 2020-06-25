package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 购物车
 */
@RestController
@RequestMapping("/frontend/cart")
public class CartController {
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @Autowired
    private CartServiceFeign cartServiceFeign;

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

    @Value("${CART_COOKIE_EXPIRE}")
    private Integer CART_COOKIE_EXPIRE;

    /**
     * 将商品添加到购物车
     * @param itemId
     * @param userId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/addItem")
    public Result addItem(Long itemId, String userId, @RequestParam(defaultValue = "1")Integer num, HttpServletRequest request, HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)) {
                //在用户未登录的状态下
                //1、从cookie中查询商品列表
                Map<String, TbItem> cart=getCartFromCookie(request);
                //2、添加商品到购物车
                addItemToCart(cart,itemId,num);
                //3、把购物商品列表写到cookie中
                addClientCookie(request,response,cart);
            }else {
                //在用户已登录的状态
                //1、从redis中查询商品列表
                Map<String,TbItem> cart=getCartFromRedis(userId);
                //2、将商品添加到大购物车上
                addItemToCart(cart,itemId,num);
                //3、将购物车缓存到redis中
                Boolean addCartToRedis=addCartToRedis(userId,cart);
                if (!addCartToRedis){
                    return Result.error("error");
                }
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }

    /**
     * 把购物车商品列表写入redis
     * @param userId
     * @param cart
     * @return
     */
    private Boolean addCartToRedis(String userId, Map<String, TbItem> cart) {
        return cartServiceFeign.insertCart(userId,cart);
    }

    /**
     * 从redis中查询购物车
     * @param userId
     * @return
     */
    private Map<String, TbItem> getCartFromRedis(String userId) {
        Map<String,TbItem> cart=cartServiceFeign.selectCartByUserId(userId);
        if (cart!=null&&cart.size()>0){
            return cart;
        }
        return new HashMap<String,TbItem>();
    }

    /**
     * 把购物商品列表写入cookie
     * @param request
     * @param response
     * @param cart
     */
    private void addClientCookie(HttpServletRequest request, HttpServletResponse response, Map<String, TbItem> cart) {
        String cartJson = JsonUtils.objectToJson(cart);
        CookieUtils.setCookie(request,response,CART_COOKIE_KEY,cartJson,CART_COOKIE_EXPIRE,true);
    }

    /**
     * 将商品添加到购物车中
     * @param cart
     * @param itemId
     * @param num
     */
    private void addItemToCart(Map<String, TbItem> cart, Long itemId, Integer num) {
        //从购物车中取商品
        TbItem tbItem = cart.get(itemId.toString());
        if (tbItem!=null){
            //商品列表中存在该商品，商品数量增加
            tbItem.setNum(tbItem.getNum()+num);
        }else {
            //商品列表中不存在该商品，根据商品id查询商品信息并添加到购物车列表
            tbItem = itemServiceFeign.selectItemInfo(itemId);
            tbItem.setNum(num);
        }
        cart.put(itemId.toString(),tbItem);
    }

    /**
     * 获取购物车
     * @param request
     * @return
     */
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cartJson = CookieUtils.getCookieValue(request, CART_COOKIE_KEY, true);
        if (StringUtils.isNotBlank(cartJson)){
            //购物车已存在
            Map<String,TbItem> map = JsonUtils.jsonToMap(cartJson, TbItem.class);
            return map;
        }
        //购物车不存在
        return new HashMap<String,TbItem>();
    }

    /**
     * 查看购物车
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/showCart")
    public Result showCart(String userId,HttpServletRequest request,HttpServletResponse response){
        List<TbItem> tbItemList = new ArrayList<>();
        try {
            if (StringUtils.isBlank(userId)) {
                //在用户未登录的状态下
                Map<String, TbItem> cart = getCartFromCookie(request);
                Set<String> keys = cart.keySet();
                for(String key:keys){
                    tbItemList.add(cart.get(key));
                }
            }else {
                //在用户已登录的状态
                Map<String, TbItem> cart = getCartFromRedis(userId);
                Set<String> keys = cart.keySet();
                for (String key:keys){
                    tbItemList.add(cart.get(key));
                }
            }
            return Result.ok(tbItemList);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.error("查询失败");
    }

    /**
     * 修改购物车
     * @param userId
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/updateItemNum")
    public Result updateItemNum(String userId,Long itemId,Integer num,HttpServletRequest request,HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                //在用户未登录的状态下
                //1、获取cookie中的购物车
                Map<String, TbItem> cart = getCartFromCookie(request);
                //2、修改购物车中的商品
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                cart.put(itemId.toString(),tbItem);
                //3、把购物车写到cookie中
                addClientCookie(request,response,cart);
            }else {
                //在用户登录的状态下
                //1、获得cookie的购物车
                Map<String, TbItem> cart = getCartFromRedis(userId);
                //2、修改购物车中的商品
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                cart.put(itemId.toString(),tbItem);
                //3、把购物车写到redis
                addCartToRedis(userId,cart);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("修改失败");
        }
    }

    /**
     * 删除购物车
     * @param itemId
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/deleteItemFromCart")
    public Result deleteItemFromCart(Long itemId,String userId,HttpServletRequest request,HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                //在用户未登录的状态
                Map<String, TbItem> cart = getCartFromCookie(request);
                cart.remove(itemId.toString());
                addClientCookie(request,response,cart);
            }else {
                //在用户登录的状态下
                //1、获得redis中的购物车
                Map<String, TbItem> cart = getCartFromRedis(userId);
                //2、删除购物车中的商品
                cart.remove(itemId.toString());
                //3、把购物车写到redis中
                addCartToRedis(userId,cart);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.error("删除失败");
    }
}
