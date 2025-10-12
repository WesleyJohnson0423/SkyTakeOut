package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 查询分类关联菜品的数量
     * @param id
     */
    @Select("select count(id) from dish where category_id = #{id}")
    Integer getCountById(Long id);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void saveWithFlavors(Dish dish);

    /**
     * 找到菜品的名字
     * @param dish
     * @return
     */
    @Select("select id from dish where name = #{name}")
    Long SelectIdByName(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> SelectByPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 查询菜品的状态
     * @param id
     * @return
     */
    @Select("select status from dish where id = #{id}")
    Integer selectStatusById(Long id);

    /**
     * 删除菜品
     * @param ids
     */
    void deleteById(List<Long> ids);

    /**
     * 改变菜品状态
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update dish set status = #{status},update_time = #{updateTime},update_user = #{updateUser} where id = #{id}")
    void changeStatus(Dish dish);

    /**
     * 数据回显，查询菜品信息
     * @param id
     * @return
     */
    @Select("select id, name, category_id, price, image, description, status, update_time from dish where id = #{id}")
    DishVO selectByDishId(Long id);

    @AutoFill(OperationType.UPDATE)
    void updateByDishId(Dish dish);

    @Select("select id from dish where category_id = #{categoryId}")
    List<Long> SelectByCategoryId(Long categoryId);

    @Select("select count(*) from dish where status = #{status}")
    Integer selectCountByStatus(Integer status);
}
