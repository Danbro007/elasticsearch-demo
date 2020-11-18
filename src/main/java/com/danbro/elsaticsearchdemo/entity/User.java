package com.danbro.elsaticsearchdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Classname User
 * @Description TODO
 * @Date 2020/11/17 13:00
 * @Author Danrbo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class User {
    private String name;
    private Integer age;
    private Date bir;
    private String content;
    private String address;

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
