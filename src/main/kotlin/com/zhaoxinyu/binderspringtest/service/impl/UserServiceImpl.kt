package com.zhaoxinyu.binderspringtest.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.zhaoxinyu.binderspringtest.bean.User
import com.zhaoxinyu.binderspringtest.mapper.UserMapper
import com.zhaoxinyu.binderspringtest.service.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl:UserService,ServiceImpl<UserMapper,User>() {
}