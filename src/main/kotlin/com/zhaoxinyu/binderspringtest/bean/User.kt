package com.zhaoxinyu.binderspringtest.bean

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.google.gson.annotations.JsonAdapter
import com.zhaoxinyu.binderspringtest.utils.DateJsonAdapter
import java.util.*

data class User(@TableId(type = IdType.AUTO) val id:Int?, var account:String, @TableField("user_name") val userName:String, val password:String,
                val gender:String?, @JsonAdapter(DateJsonAdapter::class) val birthday:Date?, @TableField("icon_path") val iconPath:String?,
                @TableField("binder_id") val binderId:Int?)
