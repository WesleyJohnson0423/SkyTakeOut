package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User selectCountByOpenId(String openid);

    void saveNewUser(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @Select("select count(id) from user where create_time < #{endTime}")
    Integer selectByDate(LocalDateTime endTime);

    @Select("select count(id) from user where create_time > #{beginTime} and create_time < #{endTime} ")
    Integer selectNewByDate(LocalDateTime beginTime, LocalDateTime endTime);
}
