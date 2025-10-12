package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 订单定时任务类
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时检查用户未支付状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void CheckOrderInUnPaid(){
        log.info("正在检查用户未支付状态:{}",new Date());
        // 获取到当前超时的订单
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> list = orderMapper.selectByStatus(Orders.UN_PAID,time);
        if (list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("付款超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 定时处理打烊时送达的餐品
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void AlreadyDelivery(){
        log.info("正在定时处理打烊时送达的餐品：{}",new Date());
        // 查找数据
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        List<Orders> list = orderMapper.selectByStatus(Orders.DELIVERY_IN_PROGRESS, time);
        // 更新
        if (list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
