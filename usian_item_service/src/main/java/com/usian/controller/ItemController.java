package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemDesc;
import com.usian.service.ItemService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    /**
     * 根据商品id查询商品信息
     * @param id
     * @return
     */
    @RequestMapping("/selectItemInfo")
    public TbItem selectItemInfo(Long id){
        return itemService.selectItemInfo(id);
    }

    /**
     * 查询所有商品并分页
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectTbItemAllByPage")
    public PageResult selectTbItemAllByPage(@RequestParam Integer page,@RequestParam Integer rows){
        return itemService.selectTbItemAllByPage(page,rows);
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/insertTbItem")
    public Integer insertTbItem(@RequestBody TbItem tbItem,@RequestParam String desc,@RequestParam String itemParams){
        return itemService.insertTbItem(tbItem,desc,itemParams);
    }

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/deleteItemById")
    Integer deleteItemById(@RequestParam Long itemId){
        return itemService.deleteItemById(itemId);
    }

    /**
     * 根据商品id查询商品描述
     * @param itemId
     * @return
     */
    @RequestMapping("/selectItemDescByItemId")
    public TbItemDesc selectItemDescByItemId(Long itemId){
        return itemService.selectItemDescByItemId(itemId);
    }
}