package com.sky.controller.user;

import com.sky.context.BaseContext;
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

@RestController("userDishController")
@Slf4j
@Api(tags = "菜品接口")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishService dishService;

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
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品信息")
    public Result<List<DishVO>> SelectByCategoryId(Long categoryId){
        log.info("用户id:{},正在查询分类：{}", BaseContext.getCurrentId(),categoryId);
        // 先判断缓存中有没有菜品数据
        String key = "dish_" +categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        // 如果存在就直接返回数据
        if (list != null && list.size() > 0){
            return Result.success(list);
        }
        // 如果不存在就进入数据库中查询，并且载入缓存中
        list = dishService.SelectByCategoryId(categoryId);
        redisTemplate.opsForValue().set(key,list);
        return Result.success(list);
    }

}
