package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user/addressBook")
@Api(tags = "地址簿相关接口")
@Slf4j
@RestController
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查看所有地址簿
     * @return
     */
    @ApiOperation("查看所有地址簿")
    @GetMapping("/list")
    public Result<List<AddressBook>> list(){
        log.info("开始查看所有地址簿");
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @ApiOperation("新增地址")
    @PostMapping
    public Result add(@RequestBody AddressBook addressBook){
        log.info("正在新增地址:{}",addressBook);
        addressBookService.add(addressBook);
        return Result.success();
    }

    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBook> selectById(@PathVariable Long id){
        log.info("开始根据id查询地址,id:{}",id);
        AddressBook addressBook = addressBookService.selectById(id);
        return Result.success(addressBook);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook){
        log.info("开始设置默认地址，id:{}",addressBook);
        Long id = addressBook.getId();
        addressBookService.setDefault(id);
        return Result.success();
    }

    /**
     * 查询默认地址
     * @return
     */
    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> selectDefault(){
        log.info("正在查询默认地址");
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }
        return Result.error("没有查询到默认地址");
    }

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    @ApiOperation("根据id修改地址")
    @PutMapping
    public Result changeAddress(@RequestBody AddressBook addressBook){
        log.info("开始修改地址:{}",addressBook);
        addressBookService.changeAddress(addressBook);
        return Result.success();
    }

    @ApiOperation("删除地址")
    @DeleteMapping
    public Result deleteById(Long id){
        log.info("正在删除地址,id：{}",id);
        addressBookService.deleteById(id);
        return Result.success();
    }
}
