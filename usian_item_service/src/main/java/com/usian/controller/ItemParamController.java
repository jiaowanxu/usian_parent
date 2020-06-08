package com.usian.controller;

import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamItem;
import com.usian.service.ItemParamService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/itemParam")
public class ItemParamController {
    @Autowired
    private ItemParamService itemParamService;

    /**
     * 根据商品分类 ID 查询规格参数模板
     * @param itemCatId
     * @return
     */
    @RequestMapping("/selectItemParamByItemCatId")
    public TbItemParam selectItemParamByItemCatId(Long itemCatId){
        return itemParamService.selectItemParamByItemCatId(itemCatId);
    }

    /**
     * 分页查询所有商品规格模板
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectItemParamAll")
    public PageResult selectItemParamAll(Integer page,Integer rows){
        return itemParamService.selectItemParamAll(page,rows);
    }

    /**
     * 添加商品规格模板
     * @param itemCatId
     * @param paramData
     * @return
     */
    @RequestMapping("/insertItemParam")
    public Integer insertItemParam(Long itemCatId,String paramData){
        return itemParamService.insertItemParam(itemCatId,paramData);
    }

    /**
     * 删除商品规格模板
     * @param id
     * @return
     */
    @RequestMapping("/deleteItemParamById")
    public Integer deleteItemParamById(Long id){
        return itemParamService.deleteItemParamById(id);
    }

    /**
     *根据商品id查询商品规格参数
     * @param itemId
     * @return
     */
    @RequestMapping("/selectTbItemParamItemByItemId")
    TbItemParamItem selectTbItemParamItemByItemId(Long itemId){
        return itemParamService.selectTbItemParamItemByItemId(itemId);
    }
}
