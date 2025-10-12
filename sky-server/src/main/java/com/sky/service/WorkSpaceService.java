package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {

    /**
     * 今日数据统计
     * @return
     * @param begin
     * @param end
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 订单数据统计
     * @return
     */
    OrderOverViewVO overviewOrders();

    /**
     * 菜品数据统计
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     * 套餐数据统计
     * @return
     */
    SetmealOverViewVO overviewSetmeals();
}
