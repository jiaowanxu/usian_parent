package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItemCat;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/itemCategory")
public class ItemCatController {
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    /**
     * 根据类目id查询当前类目的子节点
     * @param id
     * @return
     */
    @RequestMapping("/selectItemCategoryByParentId")
    public Result selectItemCategoryByParentId(@RequestParam(value = "id",defaultValue = "0") Long id){
        List<TbItemCat> tbItemCatList=itemServiceFeign.selectItemCategoryByParentId(id);
        if (tbItemCatList!=null && tbItemCatList.size()>0){
            return Result.ok(tbItemCatList);
        }
        return Result.error("查无结果");
    }
}
