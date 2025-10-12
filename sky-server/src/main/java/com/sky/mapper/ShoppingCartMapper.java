package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 添加购物车
     * @param shoppingCart
     */
    void add(ShoppingCart shoppingCart);

    /**
     * 查询购物车是否有这个菜品
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> SelectByUserIdWithDTO(ShoppingCart shoppingCart);

    /**
     * 更新购物车数据
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 查看购物车
     * @param userId
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> list(Long userId);

    /**
     * 删除单个商品
     * @param cart
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void delete(ShoppingCart cart);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clean(Long userId);

    void insertBatch(List<ShoppingCart> shoppingCarts);
}
