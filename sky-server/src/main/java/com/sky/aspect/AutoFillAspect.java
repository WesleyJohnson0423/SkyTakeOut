package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.annotation.CleanCache;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.enumeration.CleanCacheType;
import com.sky.enumeration.OperationType;
import com.sky.mapper.DishMapper;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    private static final String DISH_PATTERN = "dish_*";
    private static final String SETMEAL_PATTERN = "setMeal_*";
    private static final String CATEGORY_PATTERN = "category_*";

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillPointCut(){}

    @Pointcut("execution(* com.sky.controller.admin.*.*(..)) && @annotation(com.sky.annotation.CleanCache)")
    public void CleanCachePointCut(){}

    @Before("CleanCachePointCut()")
    public void cleanCache(JoinPoint joinPoint){
        log.info("开始自动清理缓存数据");
        // 通过链接点对象获取到指定的方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 拿到方法对应注解
        CleanCache cleanCache = signature.getMethod().getAnnotation(CleanCache.class);
        // 获取到这个注解使用的操作类型
        CleanCacheType value = cleanCache.value();
        // 拿到方法的名字
        String methodName = signature.getMethod().getName();
        // 获取到这个方法的所属类的名称
        Class<?> declaringClass = signature.getMethod().getDeclaringClass();
        String className = declaringClass.getName();
        log.info("当前执行方法的属类是：{}",className);
        if (className.contains("DishController")){
            if (value == CleanCacheType.INSERT){
                // 新增操作，只需要删除菜品所在的单一分类缓存
                // 获取方法参数
                Object[] args = joinPoint.getArgs();
                DishDTO dishDTO = (DishDTO) args[0];
                log.info("开始删除缓存");
                Long categoryId = dishDTO.getCategoryId();
                String key = "dish_" + categoryId;
                log.info("新增菜品分类id:{}",key);
                redisTemplate.delete(key);
            }
            if (value == CleanCacheType.UPDATE){
                // 更新操作删除所有缓存数据
                if (methodName.equals("ChangeStatus")){
                    log.info("该操作正在修改方法的售卖状态");
                    // 拿到方法参数，获取菜品id,然后拿到分类id，清除缓存
                    Object[] args = joinPoint.getArgs();
                    Long id = (Long) args[1];
                    DishVO dishVO = dishMapper.selectByDishId(id);
                    Long categoryId = dishVO.getCategoryId();
                    String key = "dish_" + categoryId;
                    redisTemplate.delete(key);
                }else {
                    log.info("正在更新菜品数据");
                    log.info("开始删除所有菜品缓存");
                    Set keys = redisTemplate.keys(DISH_PATTERN);
                    redisTemplate.delete(keys);
                }
            }
            if (value == CleanCacheType.DELETE){
                // 删除操作也只需要清除单一分类下的缓存
                // 获取方法参数
                Object[] args = joinPoint.getArgs();
                List<Long> ids = (List<Long>) args[0];
                // 开始删除缓存
                List<Long> categoryList = new ArrayList<>(ids.size());
                for (Long id : ids) {
                    DishVO dishVO = dishMapper.selectByDishId(id);
                    Long categoryId = dishVO.getCategoryId();
                    categoryList.add(categoryId);
                }
                for (Long id : categoryList) {
                    String key = "dish_" + id;
                    redisTemplate.delete(key);
                }
            }
        }
    }

    @Before("AutoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("自动字段填充");
        // 通过连接点对象获取到在在执行的对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 获取到方法签名
        // 拿到对应注解所使用的方法
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 获取到这个方法的操作类型
        OperationType type = autoFill.value();
        // 获得方法的参数
        Object[] pointArgs = joinPoint.getArgs();
        // 拿到实体类对象
        Object entity = pointArgs[0];
        // 获取当前时间和操作人id
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        // 对不同的操作类型使用不同的反射
        if (type == OperationType.INSERT){
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 为获得的方法对象赋值
                setCreateTime.invoke(entity,now);
                setUpdateTime.invoke(entity,now);
                setCreateUser.invoke(entity,id);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(type == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 为获得的方法对象赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
