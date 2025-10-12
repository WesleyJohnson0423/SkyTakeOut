package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetMealController")
@Slf4j
@Api(tags = "套餐管理")
@RequestMapping("/admin/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> SelectByPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("开始分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setMealService.selectByPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @CacheEvict(cacheNames = "SetMealCache",key = "#setmealDTO.categoryId")
    @Transactional
    @PostMapping
    @ApiOperation("新增套餐")
    public Result Save(@RequestBody SetmealDTO setmealDTO) {
        log.info("开始保存套餐:{}", setmealDTO);
        setMealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐的起售，停售
     *
     * @param status
     * @param id
     * @return
     */
    @CacheEvict(cacheNames = "SetMealCache",allEntries = true)
    @PostMapping("/status/{status}")
    @ApiOperation("套餐的起售，停售")
    public Result ChangeStatus(@PathVariable Integer status, Long id) {
        log.info("开始改变菜品的状态，id:{},status:{}", id, status);
        setMealService.StartOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id查询套餐数据
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询套餐数据")
    @GetMapping("/{id}")
    public Result<SetmealVO> SelectById(@PathVariable Long id) {
        log.info("正在根据id查询回显");
        SetmealVO setmealVO = setMealService.SelectById(id);
        return Result.success(setmealVO);
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @Transactional
    @DeleteMapping
    @ApiOperation("删除套餐")
    public Result DeleteByIds(@RequestParam List<Long> ids) {
        log.info("删除操作开始：{}", ids);
        setMealService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result ChangeSetMeal(@RequestBody SetmealDTO setmealDTO){
        log.info("开始更新菜品：{}",setmealDTO);
        setMealService.changeSetMeal(setmealDTO);
        return Result.success();
    }
}
