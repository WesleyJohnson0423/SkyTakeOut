package com.sky.controller.user;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
@Api(tags = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

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
}
