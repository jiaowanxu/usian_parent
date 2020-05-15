package com.usian.service;

import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;

public interface ItemService {
    TbItem selectItemInfo(Long id);

    PageResult selectTbItemAllByPage(Integer page, Integer rows);
}
