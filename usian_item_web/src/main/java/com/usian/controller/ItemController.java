package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/item")
public class ItemController {
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    /**
     * 商品信息查询
     * @param id
     * @return
     */
    @RequestMapping("/selectItemInfo")
    public Result selectItemInfo(Long id){
        TbItem tbItem = itemServiceFeign.selectItemInfo(id);
        if (tbItem != null){
            return Result.ok(tbItem);
        }else{
            return Result.error("差无结果");
        }
    }

    /**
     * 查询商品并分页处理
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectTbItemAllByPage")
    public Result selectTbItemAllByPage(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue ="2" ) Integer rows){
        PageResult pageResult=itemServiceFeign.selectTbItemAllByPage(page,rows);
        if (pageResult.getResult() != null && pageResult.getResult().size() > 0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/insertTbItem")
    public Result insertTbItem(TbItem tbItem,String desc,String itemParams){
        Integer insertTbItemNum=itemServiceFeign.insertTbItem(tbItem,desc,itemParams);
        if (insertTbItemNum==3){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/deleteItemById")
    public Result deleteItemById(Long itemId){
        Integer itemIdNum=itemServiceFeign.deleteItemById(itemId);
        if (itemIdNum==1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }
}
