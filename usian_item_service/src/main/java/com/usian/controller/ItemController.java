package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.ItemService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    //根据商品id查询商品信息
    @RequestMapping("/selectItemInfo")
    public TbItem selectItemInfo(Long id){
        return itemService.selectItemInfo(id);
    }

    //查询所有商品并分页
    public PageResult selectTbItemAllByPage(@RequestParam Integer page,@RequestParam Integer rows){
        return itemService.selectTbItemAllByPage(page,rows);
    }
}
