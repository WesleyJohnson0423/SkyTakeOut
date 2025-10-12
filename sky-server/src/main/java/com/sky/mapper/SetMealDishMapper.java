package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 查找菜品关联的套餐
     * @param id
     * @return
     */
    @Select("select count(*) from setmeal_dish where dish_id = #{id}")
    Integer selectCountByDishId(Long id);

    /**
     * 保存套餐关联的菜品
     * @param list
     */
    void saveByList(List<SetmealDish> list);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> selectByDishId(Long id);

    void deleteByIds(List<Long> ids);

    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteById(Long id);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> SelectBySetMealId(Long id);
}
