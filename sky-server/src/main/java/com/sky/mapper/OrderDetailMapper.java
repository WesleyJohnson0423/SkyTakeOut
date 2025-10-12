package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    void insert(List<OrderDetail> orderDetails);

    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> selectByOrderId(Long id);

    @Delete("delete from order_detail where order_id = #{id}")
    void delete(Long id);

    /**
     * 查询商品top10
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> selectTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
