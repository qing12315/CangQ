package com.sky.annotation;


import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,用于自动填充
 */
@Target(ElementType.METHOD)  //指定注解只能加到方法上
@Retention(RetentionPolicy.RUNTIME)  //指定注解在运行时有效
public @interface AutoFill {
    //  指定数据库操作类型：UPDATA INSERT
    OperationType value();


}
