package com.usian.service;

import com.usian.mapper.TbContentCategoryMapper;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;

    /**
     * 根据当前节点ID查询子节点
     * @param id
     * @return
     */
    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbContentCategory> tbContentCategoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        return tbContentCategoryList;
    }

    /**
     * 添加内容分类
     * @param tbContentCategory
     * @return
     */
    @Override
    public Integer insertContentCategory(TbContentCategory tbContentCategory) {
        //1、添加内容分类
        tbContentCategory.setUpdated(new Date());
        tbContentCategory.setCreated(new Date());
        tbContentCategory.setIsParent(false);
        tbContentCategory.setSortOrder(1);
        tbContentCategory.setStatus(1);
        int contentCategoryNum = tbContentCategoryMapper.insert(tbContentCategory);

        //2、如果他爹不是爹，要把他爹改成爹
        //2.1、查询当前新节点的父节点
        TbContentCategory parentId = tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());
        //2.2、判断当前父节点是否是叶子节点
        if (!parentId.getIsParent()){
            parentId.setIsParent(true);
            parentId.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKey(parentId);
        }
        return contentCategoryNum;
    }

    /**
     * 删除内容分类
     * @param categoryId
     * @return
     */
    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        //查询当前节点
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        //父节点 不允许删除
        if (tbContentCategory.getIsParent()==true){
            return 0;
        }
        //不是父节点
        tbContentCategoryMapper.deleteByPrimaryKey(categoryId);
        //当前节点的兄弟节点
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(tbContentCategory.getParentId());
        List<TbContentCategory> tbContentCategoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        //删除之后如果父节点没有子节点，则修改isParent为false
        if (tbContentCategoryList.size()==0){
            TbContentCategory parentContentCategory = new TbContentCategory();
            parentContentCategory.setId(tbContentCategory.getParentId());
            parentContentCategory.setIsParent(false);
            parentContentCategory.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKeySelective(parentContentCategory);
        }
        return 200;
    }

}
