package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FlavorMapper flavorMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setStatus(0);
        // 新增菜品
        dishMapper.saveWithFlavors(dish);
        // 获取到新增菜品的id
        Long id = dishMapper.SelectIdByName(dish);
        log.info("获取到菜品id:{}",id);
        // 新增口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            for (DishFlavor dishFlavor:flavors){
                log.info("开始注入id");
                dishFlavor.setDishId(id);
            }
            log.info("开始保存口味数据");
            flavorMapper.saveById(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult selectByPage(DishPageQueryDTO dishPageQueryDTO) {
        // 设置分页
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        // 编写SQL语句
        Page<DishVO> list = dishMapper.SelectByPage(dishPageQueryDTO);
        log.info("获取到菜品信息：{}",list);
        for (DishVO dishVO : list) {
            dishVO.setCategoryName(categoryMapper.SelectNameById(dishVO.getCategoryId()));
            dishVO.setFlavors(flavorMapper.SelectByDishId(dishVO.getId()));
        }
        // 构建PageResult类型
        PageResult pageResult = new PageResult();
        pageResult.setTotal(list.getTotal());
        pageResult.setRecords(list.getResult());
        return pageResult;
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @Override
    public void DeleteByDishId(List<Long> ids) {
        // 判断菜品是否在售卖中
        ids.forEach(id -> {
            Integer status = dishMapper.selectStatusById(id);
            if(status == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        // 查询菜品是否关键某个套餐
        ids.forEach(id ->{
            Integer count = setMealDishMapper.selectCountByDishId(id);
            if (count > 0){
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        });
        // 判断完成，删除口味以及菜品
        flavorMapper.deleteByDishId(ids);
        dishMapper.deleteById(ids);
    }

    /**
     * 改变菜品状态
     * @param status
     */
    @Override
    public void changeStatus(Integer status,Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.changeStatus(dish);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO selectByDishId(Long id) {
        DishVO dishVO = dishMapper.selectByDishId(id);
        dishVO.setCategoryName(categoryMapper.SelectNameById(id));
        dishVO.setFlavors(flavorMapper.SelectByDishId(id));
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void changeDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setStatus(0);
        // 更新菜品数据
        dishMapper.updateByDishId(dish);
        // 获取到菜品的id
        Long id = dish.getId();
        log.info("获取到菜品id:{}",id);
        // 删除原有菜品口味
        flavorMapper.deleteByOneDishId(id);
        // 更新
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            for (DishFlavor dishFlavor:flavors){
                log.info("开始注入id");
                dishFlavor.setDishId(id);
            }
            log.info("开始更新口味数据");
            flavorMapper.saveById(flavors);
        }
    }

    /**
     * 根据分类id查询菜品信息
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> SelectByCategoryId(Long categoryId) {
        List<Long> list = dishMapper.SelectByCategoryId(categoryId);
        List<DishVO> newList = new ArrayList<>(list.size());
        for (Long id : list) {
            DishVO vo = selectByDishId(id);
            newList.add(vo);
        }
        return newList;
    }

}
