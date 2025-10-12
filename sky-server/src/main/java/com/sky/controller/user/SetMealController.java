package com.sky.controller.user;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetMealController")
@Slf4j
@Api(tags = "套餐管理")
@RequestMapping("/user/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    /**
     * 根据id查询套餐数据
     *
     * @param id
     * @return
     */
    @Cacheable
    @ApiOperation("根据id查询套餐数据")
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> SelectById(@PathVariable Long id) {
        log.info("正在根据id查询回显,id:{}",id);
        List<DishItemVO> list = setMealService.SelectByDishId(id);
        return Result.success(list);
    }

    /**
     * 套餐查询
     * @param categoryId
     * @return
     */
    @Cacheable(cacheNames = "SetMealCache",key = "#categoryId")
    @ApiOperation("套餐查询")
    @GetMapping("/list")
    public Result<List<Setmeal>> SelectByCategoryId(Long categoryId){
        log.info("用户端查询套餐分类下的套餐,categoryId：{}",categoryId);
        List<Setmeal> list = setMealService.SelectByCategoryId(categoryId);
        return Result.success(list);
    }
}
