package com.usian.service;

import com.usian.pojo.SearchItem;

import java.io.IOException;
import java.util.List;

public interface SearchItemService {
    Boolean importAll();

    List<SearchItem> selectByQ(String q, Long page, Integer pageSize);

    int addDocement(String itemId) throws IOException;
}
