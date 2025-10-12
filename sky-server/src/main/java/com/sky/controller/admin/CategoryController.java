package com.sky.controller.admin;

import com.sky.annotation.CleanCache;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> Page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("菜品分页查询");
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        if (pageResult != null){
            return Result.success(pageResult);
        }else {
            return Result.error("菜品查询出现问题，请联系管理员解决");
        }
    }

    /**
     * 菜品类型分类查询
     * @param type
     * @return
     */
    @ApiOperation("菜品类型分类查询")
    @GetMapping("/list")
    public Result SelectByType(Integer type){
        log.info("菜品分类查询：{}",type);
        List<Category> categoryList = categoryService.SelectByType(type);
        return Result.success(categoryList);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @ApiOperation("新增分类")
    @PostMapping
    public Result Save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增菜品");
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @ApiOperation("根据id删除分类")
    @DeleteMapping
    public Result DeleteById(long id){
        log.info("根据id删除分类");
        categoryService.DeleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @Cacheable
    @ApiOperation("修改分类")
    @PutMapping
    public Result ChangeCategory(@RequestBody CategoryDTO categoryDTO){
        categoryService.ChangeCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    @ApiOperation("启用禁用")
    @PostMapping("/status/{status}")
    public Result ChangeStatus(@PathVariable Integer status,long id){
        categoryService.OpenOrStop(status,id);
        return Result.success();
    }
}
