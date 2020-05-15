package com.usian.feign;

import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "usian-item-service")
public interface ItemServiceFeign {
    @RequestMapping("/service/item/selectItemInfo")
    TbItem selectItemInfo(@RequestParam("id") Long id);

    @RequestMapping("/service/item/selectTbItemAllByPage")
    PageResult selectTbItemAllByPage(@RequestParam Integer page, @RequestParam Integer rows);
}
