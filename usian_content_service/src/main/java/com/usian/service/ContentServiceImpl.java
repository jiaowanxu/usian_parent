package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentExample;
import com.usian.pojo.TbItemParamExample;
import com.usian.redis.RedisClient;
import com.usian.utils.AdNode;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper tbContentMapper;

    @Autowired
    private RedisClient redisClient;

    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;

    @Value("${AD_HEIGHT}")
    private Integer AD_HEIGHT;

    @Value("${AD_WIDTH}")
    private Integer AD_WIDTH;

    @Value("${AD_HEIGHTB}")
    private Integer AD_HEIGHTB;

    @Value("${AD_WIDTHB}")
    private Integer AD_WIDTHB;

    @Value("${PORTAL_AD_REDIS_KEY}")
    private String PORTAL_AD_REDIS_KEY;

    /**
     * 根据分类ID查询分类内容
     * @param page
     * @param rows
     * @param categoryId
     * @return
     */
    @Override
    public PageResult selectTbContentAllByCategoryId(Integer page, Integer rows, Long categoryId) {
        PageHelper.startPage(page,rows);
        TbContentExample example = new TbContentExample();
        TbContentExample.Criteria criteria = example.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(tbContentList);
        PageResult pageResult = new PageResult();
        pageResult.setPageIndex(pageInfo.getPageNum());
        pageResult.setTotalPage(pageInfo.getTotal());
        pageResult.setResult(pageInfo.getList());
        return pageResult;
    }

    /**
     * 根据分类添加内容
     * @param tbContent
     * @return
     */
    @Override
    public Integer insertTbContent(TbContent tbContent) {
        tbContent.setUpdated(new Date());
        tbContent.setCreated(new Date());
        Integer num = tbContentMapper.insertSelective(tbContent);
        //缓存同步
        redisClient.hdel(PORTAL_AD_REDIS_KEY,AD_CATEGORY_ID.toString());

        return num;
    }

    /**
     * 删除分类的内容
     * @param ids
     * @return
     */
    @Override
    public Integer deleteContentByIds(Long ids) {
        Integer num=tbContentMapper.deleteByPrimaryKey(ids);
        redisClient.hdel(PORTAL_AD_REDIS_KEY,AD_CATEGORY_ID.toString());

        return num;
    }

    /**
     * 查询首页大广告
     * @return
     */
    @Override
    public List<AdNode> selectFrontendContentByAD() {
        //查询缓存
        List<AdNode> adNodeListRedis = (List<AdNode>)redisClient.hget(PORTAL_AD_REDIS_KEY,AD_CATEGORY_ID.toString());
        if (adNodeListRedis!=null){
            return adNodeListRedis;
        }
        //查询TbContent
        TbContentExample example = new TbContentExample();
        TbContentExample.Criteria criteria = example.createCriteria();
        criteria.andCategoryIdEqualTo(AD_CATEGORY_ID);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(example);
        List<AdNode> adNodeList = new ArrayList<AdNode>();
        for (TbContent tbContent:tbContentList) {
            AdNode adNode = new AdNode();
            adNode.setSrc(tbContent.getPic());
            adNode.setSrcB(tbContent.getPic2());
            adNode.setHref(tbContent.getUrl());
            adNode.setHeight(AD_HEIGHT);
            adNode.setHeightB(AD_HEIGHTB);
            adNode.setWidth(AD_WIDTH);
            adNode.setWidthB(AD_WIDTHB);
            adNodeList.add(adNode);
        }

        //添加到缓存
        redisClient.hset(PORTAL_AD_REDIS_KEY,AD_CATEGORY_ID.toString(),adNodeList);
        return adNodeList;
    }


}
