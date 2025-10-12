package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 菜品分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult selectByPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 设置分页参数
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        // 书写SQL语句，获得套餐数据
        Page<SetmealVO> page = setMealMapper.selectByPage(setmealPageQueryDTO);
        log.info("查询完毕");
        // 封装剩余数据
        for (SetmealVO setmealVO : page) {
            setmealVO.setCategoryName(categoryMapper.SelectNameById(setmealVO.getCategoryId()));
            setmealVO.setSetmealDishes(setMealDishMapper.selectByDishId(setmealVO.getId()));
        }
        // 封装PageResult对象
        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageResult.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        // 创建套餐实体类，传输数据
        Setmeal setmeal = new Setmeal();
        // 把DTO参数拷贝给实体类
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setStatus(0);
        // 将实体类参数传输到数据库
        setMealMapper.save(setmeal);
        Long id = setMealMapper.getIdByName(setmeal.getName());
        // 将菜品信息传输到数据库中
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        list.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });
        setMealDishMapper.saveByList(list);
    }

    /**
     * 套餐的起售停售
     * @param status
     * @param id
     */
    @Override
    public void StartOrStop(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setMealMapper.StartOrStop(setmeal);
    }

    /**
     * 根据id查询套餐数据
     * @param id
     * @return
     */
    @Override
    public SetmealVO SelectById(Long id) {
        SetmealVO setmealVO = setMealMapper.SelectById(id);
        List<SetmealDish> list = setMealDishMapper.SelectBySetMealId(id);
        setmealVO.setSetmealDishes(list);
        return setmealVO;
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        // 先判断该套餐是否在售
        for (Long id : ids) {
           Integer status = setMealMapper.selectStatusById(id);
           if (StatusConstant.ENABLE == status){
               throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
           }
        }
        // 判断完成，进行删除操作
        setMealDishMapper.deleteByIds(ids);
        setMealMapper.deleteByIds(ids);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void changeSetMeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        // 赋值
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setStatus(0);
        // 更新
        setMealMapper.UpdateSetMeal(setmeal);
        // 删除原有菜品关联
        setMealDishMapper.deleteById(setmeal.getId());
        // 保存新的菜品关系
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        list.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setMealDishMapper.saveByList(list);
    }

    /**
     * 用户端查询套餐分类下的套餐
     * @param id
     * @return
     */
    @Override
    public List<Setmeal> SelectByCategoryId(Long id) {
        List<Setmeal> list = setMealMapper.SelectByCategoryId(id);
        return list;
    }

    /**
     * 用户端查询套餐包含的菜品
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> SelectByDishId(Long id) {
        List<SetmealDish> dishList = setMealDishMapper.selectByDishId(id);
        List<DishItemVO> list  = new ArrayList<>(dishList.size());
        for (SetmealDish setmealDish : dishList) {
            DishVO dishVO = dishMapper.selectByDishId(setmealDish.getDishId());
            DishItemVO dishItemVO = new DishItemVO();
            BeanUtils.copyProperties(dishVO,dishItemVO);
            dishItemVO.setCopies(setmealDish.getCopies());
            list.add(dishItemVO);
        }
        return list;
    }
}
