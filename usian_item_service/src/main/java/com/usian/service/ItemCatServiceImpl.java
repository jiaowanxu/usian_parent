package com.usian.service;

import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {
    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Override
    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(id);
        criteria.andStatusEqualTo(1);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(example);
        return tbItemCatList;
    }

    /**
     * 查询首页商品分类（左侧）
     * @return
     */
    @Override
    public CatResult selectItemCategoryAll() {
        CatResult catResult = new CatResult();
        //查询商品分类
        catResult.setData(getCatList(0L));
        return catResult;
    }
    /**
     * 采用递归(私有方法）
     * 查询商品分类
     */
    public List<?> getCatList(Long parentId){
        //创建查询条件
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(example);
        List list = new ArrayList();
        int count=0;
        for (TbItemCat tbItemCat:tbItemCatList){
            //判断是否是父节点
            if (tbItemCat.getIsParent()){
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getCatList(tbItemCat.getId()));
                list.add(catNode);
                count++;
                //只取商品分类中的18条数据
                if (count==18){
                    break;
                }
            }else{
                list.add(tbItemCat.getName());
            }
        }
        return list;
    }
}
