package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 查询所有地址簿
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        List<AddressBook> list = addressBookMapper.list(addressBook);
        return list;
    }

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void add(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        // 如果一个用户还没有地址，那么这个地址就是默认地址
        // 所以因该先查询用户的地址数
        addressBook.setUserId(userId);
        List<AddressBook> bookList = addressBookMapper.list(addressBook);
        if (bookList != null && bookList.size() > 0){
            addressBook.setIsDefault(0);
        }else {
            addressBook.setIsDefault(1);
        }
        addressBookMapper.add(addressBook);
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook selectById(Long id) {
        AddressBook addressBook = addressBookMapper.selectById(id);
        return addressBook;
    }

    /**
     * 设置默认地址
     * @param id
     */
    @Override
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentId();
        // 先把这个用户的所有地址设为非默认地址,然后在单独设置默认地址
        addressBookMapper.setNotDefault(userId);
        addressBookMapper.setDefault(id);
    }

    /**
     * 根据id修改地址
     * @param addressBook
     */
    @Override
    public void changeAddress(AddressBook addressBook) {
        addressBookMapper.changeAddress(addressBook);
    }

    /**
     * 删除地址
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
