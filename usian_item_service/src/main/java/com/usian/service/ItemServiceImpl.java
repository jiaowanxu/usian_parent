package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemDescMapper;
import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisClient redisClient;

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${BASE}")
    private String BASE;

    @Value("${DESC}")
    private String DESC;

    @Value("${ITEM_INFO_EXPIRE}")
    private Long ITEM_INFO_EXPIRE;

    @Value("${SETNX_BASC_LOCK_KEY}")
    private String SETNX_BASC_LOCK_KEY;

    @Value("${SETNX_DESC_LOCK_KEY}")
    private String SETNX_DESC_LOCK_KEY;

    /**
     * 根据商品id进行商品查询
     * @param id
     * @return
     */
    @Override
    public TbItem selectItemInfo(Long id){
        //1、先查询redis，如果有就直接返回
        TbItem tbItem= (TbItem) redisClient.get(ITEM_INFO + ":" + id + ":" + BASE);
        if (tbItem!=null){
            return tbItem;
        }
        /*****************************************解决缓存击穿************************************/
        //获取锁成功后
        if (redisClient.setnx(SETNX_BASC_LOCK_KEY+":"+id,id,30L)){
            //2、查不到就再查询MySQL，并把查询结果返回到redis中
            tbItem=tbItemMapper.selectByPrimaryKey(id);
            /*****************************************解决缓存穿透************************************/
            if(tbItem == null){
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + id + ":"+ BASE,tbItem);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + id + ":"+ BASE,ITEM_INFO_EXPIRE);
            }else {
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO + ":" + id + ":"+ BASE,null);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + id + ":"+ BASE,30L);
            }
            redisClient.del(SETNX_BASC_LOCK_KEY+":"+id);
            return tbItem;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemInfo(id);
        }
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

        //添加商品发布消息到mq
        amqpTemplate.convertAndSend("item_exchange","item.add",itemId);
        return tbItemNum + tbItemDescNum + itemParamItemNum;
    }

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @Override
    public Integer deleteItemById(Long itemId) {
        return tbItemMapper.deleteByPrimaryKey(itemId);
    }

    /**
     * 根据商品id查询商品描述
     * @param itemId
     * @return
     */
    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId) {
        //查询缓存
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(ITEM_INFO + ":" + itemId + ":" + DESC);
        if (tbItemDesc!=null){
            return tbItemDesc;
        }
        /*****************************************解决缓存击穿************************************/
        if (redisClient.setnx(SETNX_DESC_LOCK_KEY+":"+itemId,itemId,30L)){
            //2、再查询mysql,并把查询结果缓存到redis
            TbItemDescExample tbItemDescExample = new TbItemDescExample();
            TbItemDescExample.Criteria criteria = tbItemDescExample.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemDesc> tbItemDescList = tbItemDescMapper.selectByExampleWithBLOBs(tbItemDescExample);
            if (tbItemDescList!=null&&tbItemDescList.size()>0){
                //1、把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,tbItemDescList.get(0));
                //2、设置缓存的失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,ITEM_INFO_EXPIRE);

                return tbItemDescList.get(0);
            }else {
                /*****************************************解决缓存穿透************************************/
                //1、把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC, null);
                //2、设置缓存的失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC, 30L);
            }
            redisClient.del(SETNX_DESC_LOCK_KEY+":"+itemId);
            return tbItemDesc;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemDescByItemId(itemId);
        }
    }
}
