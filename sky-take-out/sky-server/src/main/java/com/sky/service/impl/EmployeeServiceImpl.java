package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeEditPasswordDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
    /**
     * 新增员工
     * @paeam emloyeeDto
     */
    public void save(EmployeeDTO employeeDTO){
//        System.out.println("当前线程的id："+ Thread.currentThread().getId());

        Employee employee = new Employee();

        /** 对象属性拷贝节省代码冗余
         * employee.setId(employeeDTO.getId());
         * employee.setId(employeeDTO.getId());
         */
        BeanUtils.copyProperties(employeeDTO,employee);

        // 设置账号状态，默认值为1，1为正常，0为异常。
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码，默认密码是123456,引用PasswordConstant类中的默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录创建人ID和修改人ID

        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    // @Override
    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        // select * from employee limit 0,10
        // 开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());

        Page<Employee>page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        // updata employee set status = ? where id =?

//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);


        //跟上面方法一致
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);

        // 不让别人看到加密密码
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        // 将DTO转换成employee数据
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //动态修改属性
        employeeMapper.update(employee);
    }

    /**
     * 修改密码
     * @param employeeEditPasswordDTO
     */
    public void editPassword(EmployeeEditPasswordDTO employeeEditPasswordDTO) {

        // 根据员工ID查询员工信息
        Employee employee = employeeMapper.getById(employeeEditPasswordDTO.getId());
        if (employee == null) {
            //如果员工不存在,抛出异常
            throw new RuntimeException("员工不存在");
        }

        //  对旧密码进行MD5加密
        String oldPasswordMd5 = DigestUtils.md5DigestAsHex(employeeEditPasswordDTO.getOldPassword().getBytes());

        // 验证旧密码是否正确
        if (!oldPasswordMd5.equals(employee.getPassword())) {
            //旧密码错误,抛出异常
            throw new RuntimeException("旧密码错误");
        }

        //  对新密码进行MD5加密
        String newPasswordMd5 = DigestUtils.md5DigestAsHex(employeeEditPasswordDTO.getNewPassword().getBytes());

        //  更新密码
        employee.setPassword(newPasswordMd5);
        employeeMapper.update(employee);
    }
}
