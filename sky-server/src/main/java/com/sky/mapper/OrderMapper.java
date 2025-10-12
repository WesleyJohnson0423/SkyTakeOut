package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    OrderVO selectByOrderId(Long id);

    /**
     * 查询订单数量
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 更改状态
     * @param ordersConfirmDTO
     */
    @Update("update orders set status = #{status} where id = #{id}")
    void updateStatus(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 获取超时未支付的订单
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> selectByStatus(Integer status, LocalDateTime time);

    /**
     * 获取每日营业额
     * @param map
     * @return
     */
    Double selectAmountByMap(Map map);

    /**
     * 查询订单数据
     * @param map
     * @return
     */
    Integer selectCountByMap(Map map);

    @Select("select *")
    List<Integer> getAmountByDate(LocalDateTime beginTime, LocalDateTime endTime);
}
