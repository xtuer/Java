package com.example.demo.bean;

import java.io.Serializable;

public class Foo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int age;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
