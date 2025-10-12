package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    List<Category> SelectByType(Integer type);

    void save(CategoryDTO categoryDTO);

    void DeleteById(long id);

    void ChangeCategory(CategoryDTO categoryDTO);

    void OpenOrStop(Integer status, long id);
}
