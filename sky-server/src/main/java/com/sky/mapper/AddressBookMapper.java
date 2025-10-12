package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    List<AddressBook> list(AddressBook addressBook);

    void add(AddressBook addressBook);

    @Select("select * from address_book where id = #{id}")
    AddressBook selectById(Long id);

    @Update("update address_book set is_default = 1 where id = #{id}")
    void setDefault(Long id);

    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void setNotDefault(Long userId);

    @Select("select * from address_book where user_id = #{userId} and is_default = 1")
    AddressBook selectDefaultByUserId(Long userId);

    void changeAddress(AddressBook addressBook);

    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);

    @Select("select * from address_book where id = #{addressBookId}")
    AddressBook getById(Long addressBookId);
}
