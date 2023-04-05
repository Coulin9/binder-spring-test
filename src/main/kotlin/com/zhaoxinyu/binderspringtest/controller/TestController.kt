package com.zhaoxinyu.binderspringtest.controller

import com.zhaoxinyu.binderspringtest.bean.User
import com.zhaoxinyu.binderspringtest.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @Autowired
    lateinit var userService: UserService

    @GetMapping("/test")
    fun testSQL():List<User>{
        return userService.list()
    }
}