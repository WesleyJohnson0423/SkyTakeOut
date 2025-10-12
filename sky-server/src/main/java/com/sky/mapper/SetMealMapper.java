package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface SetMealMapper {

    /**
     * 查询分类关联菜品的数量
     * @param id
     */
    @Select("select count(id) from setmeal where category_id = #{id}")
    Integer getCountById(long id);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> selectByPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void save(Setmeal setmeal);

    /**
     * 拿到套餐的名字
     * @param name
     * @return
     */
    @Select("select id from setmeal where name = #{name}")
    Long getIdByName(String name);

    /**
     * 套餐的起售停售
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update setmeal set status = #{status},update_user = #{updateUser},update_time = #{updateTime} where id = #{id}")
    void StartOrStop(Setmeal setmeal);

    @Select("select * from setmeal where id = #{id}")
    SetmealVO SelectById(Long id);

    @Select("select status from setmeal where id = #{id}")
    Integer selectStatusById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void UpdateSetMeal(Setmeal setmeal);

    @Select("select * from setmeal where category_id = #{id} and status = 1")
    List<Setmeal> SelectByCategoryId(Long id);

    /**
     * 套餐数据统计
     * @param status
     * @return
     */
    @Select("select COUNT(*) from setmeal where status = #{status}")
    Integer selectCountByStatus(Integer status);
}
