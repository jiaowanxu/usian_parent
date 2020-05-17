package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemDescMapper;
import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.pojo.*;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private TbItemDescMapper tbItemDescMapper;
    @Autowired
    private TbItemParamItemMapper TbItemParamItemMapper;

    /**
     * 根据商品id进行商品查询
     * @param id
     * @return
     */
    @Override
    public TbItem selectItemInfo(Long id){
        return tbItemMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询所有商品，并分页
     *
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo((byte)1);
        List<TbItem> tbItemList = tbItemMapper.selectByExample(example);

        PageInfo<TbItem> PageInfo = new PageInfo<>(tbItemList);

        PageResult pageResult = new PageResult();
        pageResult.setPageIndex(page);
        pageResult.setTotalPage(PageInfo.getTotal());
        pageResult.setResult(tbItemList);
        return pageResult;
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer insertTbItem(TbItem tbItem, String desc, String itemParams) {
        //补齐Tbitem数据
        Long itemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        tbItem.setCreated(date);
        Integer tbItemNum = tbItemMapper.insertSelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        Integer tbItemDescNum = tbItemDescMapper.insertSelective(tbItemDesc);

        //补齐商品规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setUpdated(date);
        Integer itemParamItemNum = TbItemParamItemMapper.insertSelective(tbItemParamItem);

        return tbItemNum+tbItemDescNum+itemParamItemNum;
    }


}
