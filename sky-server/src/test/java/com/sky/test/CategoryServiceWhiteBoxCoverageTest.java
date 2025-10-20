package com.sky.test;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CategoryServiceImpl White Box Coverage Test
 * White Box Testing - Coverage Improvement Test
 * Goal: Improve CategoryServiceImpl code coverage to 100%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl White Box Coverage Test")
class CategoryServiceWhiteBoxCoverageTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetMealMapper setMealMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryDTO testCategoryDTO;
    private CategoryPageQueryDTO testPageQueryDTO;

    @BeforeEach
    void setUp() {
        // Prepare test data
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setType(1);
        testCategory.setSort(1);
        testCategory.setStatus(0);

        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setType(1);
        testCategoryDTO.setSort(1);

        testPageQueryDTO = new CategoryPageQueryDTO();
        testPageQueryDTO.setPage(1);
        testPageQueryDTO.setPageSize(10);
        testPageQueryDTO.setName("Test");
        testPageQueryDTO.setType(1);
    }

    /**
     * Test page() method - Pagination Query Test
     * Goal: Cover all code paths for pagination query
     */
    @Test
    @DisplayName("Should execute page query successfully")
    void testPage_Success() {
        // Given
        @SuppressWarnings("unchecked")
        Page<Category> mockPage = mock(Page.class);//Because mock result doesn't have Category generic type
        List<Category> mockList = Arrays.asList(testCategory);
        
        when(mockPage.getTotal()).thenReturn(1L);
        when(mockPage.getResult()).thenReturn(mockList);
        when(categoryMapper.pageQuery(any(CategoryPageQueryDTO.class))).thenReturn(mockPage);

        // When
        PageResult result = categoryService.page(testPageQueryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Test Category", ((Category) result.getRecords().get(0)).getName());
        
        // Verify interactions
        verify(categoryMapper).pageQuery(testPageQueryDTO);
    }

    /**
     * Test SelectByType() method - Query by Type Test
     * Goal: Cover all code paths for type-based query
     */
    @Test
    @DisplayName("Should select categories by type successfully")
    void testSelectByType_Success() {
        // Given
        Integer type = 1;
        List<Category> expectedList = Arrays.asList(testCategory);
        when(categoryMapper.SelectByType(type)).thenReturn(expectedList);

        // When
        List<Category> result = categoryService.SelectByType(type);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getName());
        assertEquals(type, result.get(0).getType());
        
        // Verify interactions
        verify(categoryMapper).SelectByType(type);
    }

    /**
     * Test save() method - Add Category Test
     * Goal: Cover all code paths for adding new category
     */
    @Test
    @DisplayName("Should save new category successfully")
    void testSave_Success() {
        // Given
        doNothing().when(categoryMapper).save(any(Category.class));

        // When
        categoryService.save(testCategoryDTO);

        // Then
        verify(categoryMapper).save(argThat(category -> 
            "Test Category".equals(category.getName()) &&
            category.getType() == 1 &&
            category.getSort() == 1 &&
            category.getStatus() == 0
        ));
    }

    /**
     * Test DeleteById() method - Delete Category Success Test
     * Goal: Cover successful deletion path for category
     */
    @Test
    @DisplayName("Should delete category successfully when no related dishes or setmeals")
    void testDeleteById_Success() {
        // Given
        Long categoryId = 1L;
        when(dishMapper.getCountById(categoryId)).thenReturn(0);
        when(setMealMapper.getCountById(categoryId)).thenReturn(0);
        doNothing().when(categoryMapper).DeleteById(categoryId);

        // When
        categoryService.DeleteById(categoryId);

        // Then
        verify(dishMapper).getCountById(categoryId);
        verify(setMealMapper).getCountById(categoryId);
        verify(categoryMapper).DeleteById(categoryId);
    }

    /**
     * Test DeleteById() method - Delete Category Failure Test (Related Dishes)
     * Goal: Cover exception path when deleting category with related dishes
     */
    @Test
    @DisplayName("Should throw exception when deleting category with related dishes")
    void testDeleteById_WithRelatedDishes_ThrowsException() {
        // Given
        Long categoryId = 1L;
        when(dishMapper.getCountById(categoryId)).thenReturn(1);

        // When & Then
        DeletionNotAllowedException exception = assertThrows(
            DeletionNotAllowedException.class,
            () -> categoryService.DeleteById(categoryId)
        );

        assertEquals(MessageConstant.CATEGORY_BE_RELATED_BY_DISH, exception.getMessage());
        verify(dishMapper).getCountById(categoryId);
        verify(setMealMapper, never()).getCountById(anyLong());
        verify(categoryMapper, never()).DeleteById(anyLong());
    }

    /**
     * Test DeleteById() method - Delete Category Failure Test (Related Setmeals)
     * Goal: Cover exception path when deleting category with related setmeals
     */
    @Test
    @DisplayName("Should throw exception when deleting category with related setmeals")
    void testDeleteById_WithRelatedSetmeals_ThrowsException() {
        // Given
        Long categoryId = 1L;
        when(dishMapper.getCountById(categoryId)).thenReturn(0);
        when(setMealMapper.getCountById(categoryId)).thenReturn(1);

        // When & Then
        DeletionNotAllowedException exception = assertThrows(
            DeletionNotAllowedException.class,
            () -> categoryService.DeleteById(categoryId)
        );

        assertEquals(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL, exception.getMessage());
        verify(dishMapper).getCountById(categoryId);
        verify(setMealMapper).getCountById(categoryId);
        verify(categoryMapper, never()).DeleteById(anyLong());
    }

    /**
     * Test ChangeCategory() method - Modify Category Test
     * Goal: Cover all code paths for modifying category
     */
    @Test
    @DisplayName("Should change category successfully")
    void testChangeCategory_Success() {
        // Given
        doNothing().when(categoryMapper).ChangeCategory(any(Category.class));

        // When
        categoryService.ChangeCategory(testCategoryDTO);

        // Then
        verify(categoryMapper).ChangeCategory(argThat(category -> 
            "Test Category".equals(category.getName()) &&
            category.getType() == 1 &&
            category.getSort() == 1
        ));
    }

    /**
     * Test OpenOrStop() method - Enable/Disable Test
     * Goal: Cover all code paths for enable/disable functionality
     * Note: Simplified handling due to static method mocking limitations
     */
    @Test
    @DisplayName("Should change category status successfully")
    void testOpenOrStop_Success() {
        // Given
        Integer status = 1;
        Long categoryId = 1L;
        doNothing().when(categoryMapper).OpenOrStop(anyInt(), anyLong(), any(), any(LocalDateTime.class));

        // When
        categoryService.OpenOrStop(status, categoryId);

        // Then
        verify(categoryMapper).OpenOrStop(eq(status), eq(categoryId), any(), any(LocalDateTime.class));
    }

    /**
     * Test OpenOrStop() method - Disable Category Test
     * Goal: Cover code path for disabling category
     * Note: Simplified handling due to static method mocking limitations
     */
    @Test
    @DisplayName("Should disable category successfully")
    void testOpenOrStop_Disable_Success() {
        // Given
        Integer status = 0; // Disable
        Long categoryId = 1L;
        doNothing().when(categoryMapper).OpenOrStop(anyInt(), anyLong(), any(), any(LocalDateTime.class));

        // When
        categoryService.OpenOrStop(status, categoryId);

        // Then
        verify(categoryMapper).OpenOrStop(eq(status), eq(categoryId), any(), any(LocalDateTime.class));
    }

    /**
     * Test page() method with empty result - Empty Result Test
     * Goal: Cover code path for pagination query returning empty result
     */
    @Test
    @DisplayName("Should handle empty page result")
    void testPage_EmptyResult() {
        // Given
        @SuppressWarnings("unchecked")
        Page<Category> mockPage = mock(Page.class);
        when(mockPage.getTotal()).thenReturn(0L);
        when(mockPage.getResult()).thenReturn(Arrays.asList());
        when(categoryMapper.pageQuery(any(CategoryPageQueryDTO.class))).thenReturn(mockPage);

        // When
        PageResult result = categoryService.page(testPageQueryDTO);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getRecords().size());
        
        verify(categoryMapper).pageQuery(testPageQueryDTO);
    }

    /**
     * Test SelectByType() method with empty result - Empty Type Query Test
     * Goal: Cover code path for type-based query returning empty result
     */
    @Test
    @DisplayName("Should handle empty type query result")
    void testSelectByType_EmptyResult() {
        // Given
        Integer type = 2;
        when(categoryMapper.SelectByType(type)).thenReturn(Arrays.asList());

        // When
        List<Category> result = categoryService.SelectByType(type);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(categoryMapper).SelectByType(type);
    }
}
