package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 员工状态调整
     * @param status
     * @param id
     * @return
     */
    void changeStatus(Integer status, long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    void changeInformation(EmployeeDTO employeeDTO);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(long id);

    /**
     * 更改密码
     * @param passwordEditDTO
     * @return
     */
    Boolean changePassword(PasswordEditDTO passwordEditDTO);
}
