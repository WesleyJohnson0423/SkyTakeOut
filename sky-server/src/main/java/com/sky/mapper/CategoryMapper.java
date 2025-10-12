package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 菜品分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 菜品分类查询
     * @param type
     * @return
     */
    List<Category> SelectByType(Integer type);

    /**
     * 新增分类
     * @param category
     */
    @AutoFill(OperationType.INSERT)
    void save(Category category);

    /**
     * 根据id删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void DeleteById(long id);

    /**
     * 修改分类
     * @param category
     */
    @AutoFill(OperationType.UPDATE)
    void ChangeCategory(Category category);

    /**
     * 启用禁用
     * @param status
     * @param id
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update category set status = #{status} , update_user = #{currentId} , update_time = #{now} where id = #{id}")
    void OpenOrStop(Integer status, long id , Long currentId , LocalDateTime now);

    @Select("select name from category where id = #{id}")
    String SelectNameById(Long id);

}
