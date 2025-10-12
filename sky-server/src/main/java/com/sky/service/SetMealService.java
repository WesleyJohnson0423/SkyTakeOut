package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetMealService {
    PageResult selectByPage(SetmealPageQueryDTO setmealPageQueryDTO);

    void save(SetmealDTO setmealDTO);

    void StartOrStop(Integer status, Long id);

    SetmealVO SelectById(Long id);

    void deleteByIds(List<Long> ids);

    void changeSetMeal(SetmealDTO setmealDTO);

    List<Setmeal> SelectByCategoryId(Long id);

    List<DishItemVO> SelectByDishId(Long id);
}
