package com.zhaoxinyu.binderspringtest

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@MapperScan("com.zhaoxinyu.binderspringtest.mapper")
@SpringBootApplication
class BinderSpringTestApplication

fun main(args: Array<String>) {
    runApplication<BinderSpringTestApplication>(*args)
}
