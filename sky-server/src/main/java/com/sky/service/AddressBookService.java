package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    List<AddressBook> list(AddressBook addressBook);

    void add(AddressBook addressBook);

    AddressBook selectById(Long id);

    void setDefault(Long id);

    void changeAddress(AddressBook addressBook);

    void deleteById(Long id);
}
