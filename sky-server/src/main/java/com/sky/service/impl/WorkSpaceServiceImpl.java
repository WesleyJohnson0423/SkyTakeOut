package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 今日数据统计
     * @return
     * @param begin
     * @param end
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // 获取到今日时间
//        LocalDate localDate = LocalDate.now();
//        LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
//        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
        Map map = new HashMap();
        map.put("beginTime",begin);
        map.put("endTime",end);
        // 获取所有订单数
        Integer totalOrderCount = orderMapper.selectCountByMap(map);
        totalOrderCount = totalOrderCount == null ? 0 : totalOrderCount;
        // 获取有效订单
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.selectCountByMap(map);
        validOrderCount = validOrderCount == null ? 0 : validOrderCount;
        // 今日营业额查询
        Double turnOver = orderMapper.selectAmountByMap(map);
        turnOver = turnOver == null ? 0.0 : turnOver;
        // 获得订单完成率
        Double orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        if (validOrderCount == 0){
            orderCompletionRate = 0.0;
        }
        // 获取平均客单价
        Double unitPrice = turnOver / validOrderCount;
        if (turnOver == 0){
            unitPrice = 0.0;
        }
        // 获取新增用户数
        Integer newUsers = userMapper.selectNewByDate(begin, end);
        // 封装
        return BusinessDataVO
                .builder()
                .turnover(turnOver)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
//        Map map = new HashMap();
//        map.put("begin",begin);
//        map.put("end",end);
//
//        //查询总订单数
//        Integer totalOrderCount = orderMapper.selectCountByMap(map);
//
//        map.put("status", Orders.COMPLETED);
//        //营业额
//        Double turnover = orderMapper.selectAmountByMap(map);
//        turnover = turnover == null? 0.0 : turnover;
//
//        //有效订单数
//        Integer validOrderCount = orderMapper.selectCountByMap(map);
//
//        Double unitPrice = 0.0;
//
//        Double orderCompletionRate = 0.0;
//        if(totalOrderCount != 0 && validOrderCount != 0){
//            //订单完成率
//            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
//            //平均客单价
//            unitPrice = turnover / validOrderCount;
//        }
//
//        //新增用户数
//        Integer newUsers = userMapper.selectNewByDate(begin,end);
//
//        return BusinessDataVO.builder()
//                .turnover(turnover)
//                .validOrderCount(validOrderCount)
//                .orderCompletionRate(orderCompletionRate)
//                .unitPrice(unitPrice)
//                .newUsers(newUsers)
//                .build();
    }

    /**
     * 订单数据统计
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        // 获取到今日时间
        LocalDate localDate = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
        Map map = new HashMap();
        map.put("beginTime",beginTime);
        map.put("endTime",endTime);
        // 获取到全部订单
        Integer allOrders = orderMapper.selectCountByMap(map);
        //获取到待接单订单
        map.put("status",Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.selectCountByMap(map);
        // 获取到带派送订单
        map.replace("status",Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.selectCountByMap(map);
        // 获取到已取消订单
        map.replace("status",Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.selectCountByMap(map);
        // 获取到已完成订单
        map.replace("status",Orders.COMPLETED);
        Integer completedOrders = orderMapper.selectCountByMap(map);
        return OrderOverViewVO
                .builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 菜品数据统计
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        // 获取到已起售数量
        Integer sold = dishMapper.selectCountByStatus(1);
        // 获取到已停售数量
        Integer discontinued = dishMapper.selectCountByStatus(0);
        return DishOverViewVO
                .builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 套餐数据统计
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        // 获取到已起售数量
        Integer sold = setMealMapper.selectCountByStatus(1);
        // 获取到已停售数量
        Integer discontinued = setMealMapper.selectCountByStatus(0);

        return SetmealOverViewVO
                .builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
