package com.zhaoxinyu.binderspringtest.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.zhaoxinyu.binderspringtest.bean.DiaryVersion
import com.zhaoxinyu.binderspringtest.mapper.DiaryVersionMapper
import com.zhaoxinyu.binderspringtest.service.DiaryVersionService
import org.springframework.stereotype.Service

@Service
class DiaryVersionServiceImpl:DiaryVersionService,ServiceImpl<DiaryVersionMapper,DiaryVersion>() {
}