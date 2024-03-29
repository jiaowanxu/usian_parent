package com.usian.controller;

import com.usian.feign.SearchItemFeign;
import com.usian.pojo.SearchItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/frontend/searchItem")
public class SearchItemController {
    @Autowired
    private SearchItemFeign searchItemFeign;

    @RequestMapping("/importAll")
    public Boolean importAll(){
        return searchItemFeign.importAll();
    }

    @RequestMapping("/list")
    public List<SearchItem> selectByQ(String q, @RequestParam(defaultValue = "1")Long page,@RequestParam(defaultValue = "20")Integer pageSize){
        return searchItemFeign.selectByQ(q,page,pageSize);
    }
}
