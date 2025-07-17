package com.sky.dto;
import lombok.Data;

import java.io.Serializable;
import lombok.Data;

@Data

public class EmployeeEditPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;  //员工ID
    private String oldPassword;  //旧密码
    private String newPassword;  //新密码
}
