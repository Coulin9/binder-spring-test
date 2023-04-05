package com.zhaoxinyu.binderspringtest.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.zhaoxinyu.binderspringtest.bean.Diary
import com.zhaoxinyu.binderspringtest.mapper.DiaryMapper
import com.zhaoxinyu.binderspringtest.service.DiaryService
import org.springframework.stereotype.Service

@Service
class DiaryServiceImpl:DiaryService,ServiceImpl<DiaryMapper,Diary>() {
}