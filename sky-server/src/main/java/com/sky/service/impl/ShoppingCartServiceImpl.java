package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetMealMapper setMealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 我们需要先判断购物车数据库中是否有我们传进来的购物车DTO对象，
        // 如果没有的话就需要进行一个插入操作，如果有的话就要进行一个更新操作，更新菜品的数量和价钱
        // 我们现在有了菜品id和套餐id，我们需要拿到用户的id并且根据菜品的id来查询菜品的数据
        // 新建购物车和菜品对象，方便下一步的插入和更新操作
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.SelectByUserIdWithDTO(shoppingCart);
        //如果获取到的数据不为空，那么就数量+1,更新到数据库中
        if (list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }
        // 如果商品不存在，那么就加入购物车
        else {
            // 如果前端传回来的是套餐对象,那么就需要封装套餐对象，并传入我们的购物车对象中
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId == null){
                SetmealVO setmealVO = setMealMapper.SelectById(setmealId);
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setAmount(setmealVO.getPrice());
            }else {
                DishVO dishVO = dishMapper.selectByDishId(dishId);
                shoppingCart.setImage(dishVO.getImage());
                shoppingCart.setName(dishVO.getName());
                shoppingCart.setAmount(dishVO.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.add(shoppingCart);
        }

    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(userId);
        return list;
    }

    /**
     * 删除单个商品
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        // 先判断商品的数量是多少
        // 如果商品的数量大于一那么执行的就是更新操作
        // 如果商品的数量等于一那么执行的就是删除操作
        Long userId = BaseContext.getCurrentId();
        // 新建购物车对象，进行赋值
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);
        // 搜索商品数量
        List<ShoppingCart> list = shoppingCartMapper.SelectByUserIdWithDTO(shoppingCart);
        ShoppingCart cart = list.get(0);
        // 判断商品数量
        if (cart.getNumber() > 1){
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            shoppingCartMapper.delete(cart);
        }
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }


}
