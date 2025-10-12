package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Api(tags = "工作台相关接口")
@RestController
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 今日数据统计
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("今日数据统计")
    public Result<BusinessDataVO> getBusinessData(){
        log.info("正在获取今日数据:{}",new Date());
        LocalDate date = LocalDate.now();
        LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(begin,end);
        return Result.success(businessDataVO);
    }

    /**
     * 订单数据统计
     * @return
     */
    @ApiOperation("订单数据统计")
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("正在统计订单数据:{}",new Date());
        OrderOverViewVO orderOverViewVO = workSpaceService.overviewOrders();
        return Result.success(orderOverViewVO);
    }

    /**
     * 菜品数据统计
     * @return
     */
    @ApiOperation("菜品数据统计")
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes(){
        DishOverViewVO dish = workSpaceService.overviewDishes();
        return Result.success(dish);
    }

    /**
     * 套餐数据统计
     * @return
     */
    @ApiOperation("套餐数据统计")
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        SetmealOverViewVO setmealOverViewVO = workSpaceService.overviewSetmeals();
        return Result.success(setmealOverViewVO);
    }
}
