package com.zhaoxinyu.binderspringtest.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.google.gson.Gson
import com.zhaoxinyu.binderspringtest.bean.Diary
import com.zhaoxinyu.binderspringtest.bean.DiaryVersion
import com.zhaoxinyu.binderspringtest.bean.Result
import com.zhaoxinyu.binderspringtest.service.DiaryService
import com.zhaoxinyu.binderspringtest.service.DiaryVersionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class DiaryController {

    @Autowired
    private lateinit var diaryService: DiaryService

    @Autowired
    private lateinit var diaryVersionService: DiaryVersionService

    /**
     * 新增日记
     */
    @PostMapping("/addDiary")
    fun addDiary(@RequestBody diary: Diary):Result{
        try{
            val oldVersion=diaryVersionService.getOne(QueryWrapper<DiaryVersion>().eq("user_id",diary.userId))
            var vUpdate=false
            var version=-1
            if(oldVersion==null){
                vUpdate=diaryVersionService.save(DiaryVersion(diary.userId,1))
                version=1
            }else {
                val newVersion=oldVersion.version+1
                vUpdate=diaryVersionService.update(UpdateWrapper<DiaryVersion>().eq("user_id",diary.userId).set("version",newVersion))
                version=newVersion
            }
            if(diaryService.save(diary)&&vUpdate){
                return Result(true,"Add Dairy Success!",version)
            }else{
                return Result(false,"Add Diary Failed!")
            }
        }catch (e:Exception){
            return Result(false,"Add Diary Failed! ${e.message}")
        }
    }

    /**
     * 修改日记
     */
    @PostMapping("/updateDiary")
    fun updateDiary(@RequestBody diary: Diary):Result{
        try {
            val oldVersion=diaryVersionService.getOne(QueryWrapper<DiaryVersion>().eq("user_id",diary.userId)).version
            val newVersion=oldVersion+1
            if(diaryService.update(diary,UpdateWrapper<Diary>().eq("id",diary.id))
                &&diaryVersionService.update(UpdateWrapper<DiaryVersion>().eq("user_id",diary.userId).set("version",newVersion))){
                return Result(true,"Update Diary Success!",newVersion)
            }else{
                return Result(false,"Update Diary Failed!")
            }
        }catch (e:Exception){
            return Result(false,"Update Diary Failed! ${e.message}")
        }
    }

    /**
     * 查询某用户的全部日记，并按日期从大到小排序
     */
    @GetMapping("/getAllDiary")
    fun getAllDiary(@RequestParam("userId") userId:Int):Result{
        try {
            val list=diaryService.list(QueryWrapper<Diary>().eq("user_id",userId).orderBy(true,false,"date"))
            if(list!=null){
                return Result(true,"Get All Diary Success!",Gson().toJson(list))
            }else return Result(false,"Get All Diary Failed!",userId)
        }catch (e:Exception){
            return Result(false,"Get All Diary Failed! ${e.message}",userId)
        }
    }

    /**
     * 查询某用户的最新n篇日记
     */
    @GetMapping("/getNewNDiary")
    fun getNewNDiary(@RequestParam("userId") userId: Int,@RequestParam("count") count:Int):Result{
        try {
            val list=diaryService.list(QueryWrapper<Diary>().eq("user_id",userId).orderBy(true,false,"date").last("LIMIT ${count}"))
            if(list!=null){
                return Result(true,"Get New N Diary Success!",Gson().toJson(list))
            }else return Result(false,"Get New N Diary Failed!",userId)
        }catch (e:Exception){
            return Result(false,"Get New N Diary Failed! ${e.message}",userId)
        }
    }

    /**
     * 删除某篇日记
     */
    @PostMapping("/deleteOneDiary")
    fun deleteOneDairy(@RequestBody diary: Diary):Result{
        try {
            val oldVersion=diaryVersionService.getOne(QueryWrapper<DiaryVersion>().eq("user_id",diary.userId))
            val newVersion=oldVersion.version+1
            if(diaryService.removeById(diary)
                &&diaryVersionService.update(UpdateWrapper<DiaryVersion>().eq("user_id",diary.userId).set("version",newVersion))){
                return Result(true,"Delete One Diary Success!",newVersion)
            }else return Result(false,"Delete One Diary Failed!")
        }catch (e:Exception){
            return Result(false,"Delete One Diary Failed! ${e.message}")
        }
    }

    /**
     * 删除某用户所有的日记
     */
    @GetMapping("/deleteAllDiary")
    fun deleteAllDiary(@RequestParam("userId") userId: Int):Result{
        try {
            val oldVersion=diaryVersionService.getOne(QueryWrapper<DiaryVersion>().eq("user_id",userId))
            val newVersion=oldVersion.version+1
            if(diaryService.remove(QueryWrapper<Diary>().eq("user_id",userId))
                &&diaryVersionService.update(UpdateWrapper<DiaryVersion>().eq("user_id",userId).set("version",newVersion))){
                return Result(true,"Delete All Diary Success!")
            }else return Result(false,"Delete All Diary Failed!",userId)
        }catch (e:Exception){
            return Result(false,"Delete All Diary Failed! ${e.message}",userId)
        }
    }

    /**
     * 获取相应用户的日记版本号
     */
    @GetMapping("/getDairyVersion")
    fun getDiaryVersion(@RequestParam("userId") userId: Int):Result{
        try {
            val version=diaryVersionService.getOne(QueryWrapper<DiaryVersion>().eq("user_id",userId)).version
            if(version==null){
                return Result(false,"Get Diary Version Failed!",userId)
            }else return Result(true,"Get Diary Version Success!",version)
        }catch (e:Exception){
            return Result(false,"Get Diary Version Failed! ${e.message}",userId)
        }
    }
}