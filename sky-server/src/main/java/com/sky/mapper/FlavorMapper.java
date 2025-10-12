package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlavorMapper {

    /**
     * 保存菜品
     * @param flavors
     */
    void saveById(List<DishFlavor> flavors);

    /**
     * 查询菜品id
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> SelectByDishId(Long id);

    /**
     *
     * @param ids
     */
    void deleteByDishId(List<Long> ids);

    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByOneDishId(Long id);

}
