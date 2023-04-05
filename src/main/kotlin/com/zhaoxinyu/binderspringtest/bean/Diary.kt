package com.zhaoxinyu.binderspringtest.bean

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.google.gson.annotations.JsonAdapter
import com.zhaoxinyu.binderspringtest.utils.DateJsonAdapter
import java.util.*

@TableName("diary")
data class Diary(@TableId(type = IdType.AUTO) val id:Int?, @TableField("user_id") val userId:Int,
                 @TableField("date") @JsonAdapter(DateJsonAdapter::class) val date:Date, @TableField("content") val content:String)
