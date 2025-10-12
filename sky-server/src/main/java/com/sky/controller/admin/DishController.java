package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.annotation.CleanCache;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.CleanCacheType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@Slf4j
@Api(tags = "菜品接口")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @CleanCache(CleanCacheType.INSERT)
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("开始保存菜品");
        dishService.save(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> selectByPage(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询开始:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.selectByPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @return
     */
    @CleanCache(CleanCacheType.DELETE)
    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result DeleteByDishId(@RequestParam List<Long> ids){
        log.info("开始删除菜品");
        dishService.DeleteByDishId(ids);
        return Result.success();
    }

    /**
     * 改变状态
     * @param status
     * @return
     */
    @CleanCache(CleanCacheType.UPDATE)
    @PostMapping("/status/{status}")
    @ApiOperation("改变状态")
    public Result ChangeStatus(@PathVariable Integer status,Long id){
        log.info("开始改变菜品：{},状态：{}",id,status);
        dishService.changeStatus(status,id);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> selectByDishId(@PathVariable Long id){
        log.info("开始查询菜品：{}",id);
        DishVO dishVO = dishService.selectByDishId(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @CleanCache(CleanCacheType.UPDATE)
    @PutMapping
    @ApiOperation("修改菜品")
    public Result ChangeDish(@RequestBody DishDTO dishDTO){
        log.info("开始修改菜品");
        dishService.changeDish(dishDTO);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品信息")
    public Result<List<DishVO>> SelectByCategoryId(Long categoryId){
        List<DishVO> list = dishService.SelectByCategoryId(categoryId);
        return Result.success(list);
    }
}
