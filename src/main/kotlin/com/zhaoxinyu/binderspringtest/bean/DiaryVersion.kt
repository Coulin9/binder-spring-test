package com.zhaoxinyu.binderspringtest.bean

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

@TableName("diary_version")
data class DiaryVersion(@TableId("user_id") val userId:Int, @TableField("version") val version:Int)
