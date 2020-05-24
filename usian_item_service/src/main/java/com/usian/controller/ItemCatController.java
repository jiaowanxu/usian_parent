package com.usian.controller;

import com.usian.pojo.TbItemCat;
import com.usian.service.ItemCatService;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/itemCategory")
public class ItemCatController {
    @Autowired
    private ItemCatService itemCatService;

    @RequestMapping("/selectItemCategoryByParentId")
    public List<TbItemCat> selectItemCategoryByParentId(@RequestParam Long id){
        return itemCatService.selectItemCategoryByParentId(id);
    }

    /**
     * 查询首页商品分类（左侧）
     * @return
     */
    @RequestMapping("/selectItemCategoryAll")
    public CatResult selectItemCategoryAll(){
        return itemCatService.selectItemCategoryAll();
    }
}
