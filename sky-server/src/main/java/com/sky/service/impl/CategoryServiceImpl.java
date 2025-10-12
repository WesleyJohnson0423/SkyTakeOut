package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 菜品分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 使用分页查询插件动态分页
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        //使用SQL语句查询数据
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        // 拿到总记录数和数据，封装到PageResult对象中
        long total = page.getTotal();
        List<Category> list = page.getResult();

        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(list);
        return pageResult;
    }

    /**
     * 菜品类型查询
     * @param type
     * @return
     */
    @Override
    public List<Category> SelectByType(Integer type) {
        List<Category> categoryList = categoryMapper.SelectByType(type);
        return  categoryList;
    }

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(0);
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.save(category);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void DeleteById(long id) {
        // 查询是否关联了菜品
        Integer dishNum = dishMapper.getCountById(id);
        if(dishNum > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        // 查询是否关联了套餐
        dishNum = setMealMapper.getCountById(id);
        if(dishNum > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.DeleteById(id);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void ChangeCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 将DTO对象转换为实体类
        BeanUtils.copyProperties(categoryDTO,category);
        // 添加修改人和修改实践
//        category.setUpdateUser(BaseContext.getCurrentId());
//        category.setUpdateTime(LocalDateTime.now());
        // 使用SQL语句修改分类
        categoryMapper.ChangeCategory(category);
    }

    /**
     * 启用禁用
     * @param status
     * @param id
     */
    @Override
    public void OpenOrStop(Integer status, long id) {
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        categoryMapper.OpenOrStop(status,id,currentId,now);
    }
}
