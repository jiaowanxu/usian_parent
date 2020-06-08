package com.usian.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.mapper.TbItemParamMapper;
import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamExample;
import com.usian.pojo.TbItemParamItem;
import com.usian.pojo.TbItemParamItemExample;
import com.usian.redis.RedisClient;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {
    @Autowired
    private TbItemParamMapper tbItemParamMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Autowired
    private RedisClient redisClient;

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${PARAM}")
    private String PARAM;

    @Value("${ITEM_INFO_EXPIRE}")
    private Long ITEM_INFO_EXPIRE;

    @Value("${SETNX_PARAM_LOCK_KEY}")
    private String SETNX_PARAM_LOCK_KEY;

    /**
     * 根据商品分类 ID 查询规格参数模板
     * @param itemCatId
     * @return
     */
    @Override
    public TbItemParam selectItemParamByItemCatId(Long itemCatId) {
        TbItemParamExample example = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = example.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> tbItemParamList = tbItemParamMapper.selectByExampleWithBLOBs(example);
        if (tbItemParamList!=null && tbItemParamList.size()>0){
            return tbItemParamList.get(0);
        }
        return null;
    }

    /**
     * 分页查询所有商品规格模板
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectItemParamAll(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        TbItemParamExample example = new TbItemParamExample();
        example.setOrderByClause("updated DESC");

        List<TbItemParam> tbItemParamList = tbItemParamMapper.selectByExampleWithBLOBs(example);
        PageInfo<TbItemParam> pageInfo = new PageInfo<>(tbItemParamList);
        PageResult pageResult = new PageResult();
        pageResult.setPageIndex(page);
        pageResult.setResult(pageInfo.getList());
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));
        return pageResult;
    }

    /**
     * 添加商品规格模板
     * @param itemCatId
     * @param paramData
     * @return
     */
    @Override
    public Integer insertItemParam(Long itemCatId, String paramData) {
        //1、判断该类别的商品是否有规格模板
        TbItemParamExample tbItemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = tbItemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> tbItemParamList = tbItemParamMapper.selectByExample(tbItemParamExample);
        if (tbItemParamList.size()>0){
            return 0;
        }

        //2、保存规格模板
        Date date = new Date();
        TbItemParam tbItemParam = new TbItemParam();
        tbItemParam.setItemCatId(itemCatId);
        tbItemParam.setParamData(paramData);
        tbItemParam.setCreated(date);
        tbItemParam.setUpdated(date);

        return tbItemParamMapper.insertSelective(tbItemParam);
    }

    /**
     * 删除商品规格模板
     * @param id
     * @return
     */
    @Override
    public Integer deleteItemParamById(Long id) {
        return tbItemParamMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据商品id查询商品规格参数
     * @param itemId
     * @return
     */
    @Override
    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId) {
        //1、先查询redis，如果有就直接返回
        TbItemParamItem tbItemParamItem = (TbItemParamItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + PARAM);
        if (tbItemParamItem!=null){
            return tbItemParamItem;
        }
        /*****************************************解决缓存击穿************************************/
        if (redisClient.setnx(SETNX_PARAM_LOCK_KEY+":"+itemId,itemId,30L)){
            //2、查不到就再查询MySQL，并把查询结果返回到redis中
            TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
            TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemParamItem> tbItemParamItemList = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
            if (tbItemParamItemList!=null&&tbItemParamItemList.size()>0){
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM,tbItemParamItemList.get(0));
                //设置缓存的失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM,ITEM_INFO_EXPIRE);
                return tbItemParamItemList.get(0);
            }else {
                /*****************************************解决缓存穿透************************************/
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM, null);
                //设置缓存的失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM, 30L);
            }
            redisClient.del(SETNX_PARAM_LOCK_KEY+":"+itemId);
            return tbItemParamItem;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectTbItemParamItemByItemId(itemId);
        }
    }
}
