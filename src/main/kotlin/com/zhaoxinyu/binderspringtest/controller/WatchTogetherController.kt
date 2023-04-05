package com.zhaoxinyu.binderspringtest.controller

import com.google.gson.Gson
import com.zhaoxinyu.binderspringtest.bean.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*
import java.math.RoundingMode

@RestController
class WatchTogetherController {


    //获取新消息
    @PostMapping("/get_new_message")
    fun getNewMessage(@RequestBody user: User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        val msg=room?.getNewMessage(user)
        return if(msg==null) Result(false,"Invalid User Or No New Message!",null)
        else Result(true,"GetNewMessageSuccess!",msg)
    }

    //发送一条消息
    @PostMapping("/send_message")
    fun sendMessage(@RequestBody user: User?,@RequestParam("msg") msg:String):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        return if(room==null||!room.sendMessage(user,msg)) Result(false,"Invalid User!",null)
        else Result(true,"Send Success!",msg)
    }



    //开启放映，这里请求体是一个map，用json字符串的形式保存了用户和媒体信息
    @PostMapping("/start_streaming")
    fun startStreaming(@RequestBody map:Map<String,String>):Result{
        //println(map)
        val user= Gson().fromJson(map["user"],User::class.java)
        val mInfo=Gson().fromJson(map["mInfo"],MediaInfo::class.java)
        if(user==null||mInfo==null) return Result(false,"Null User Or MediaInfo!",null)
        val room=RoomStore.getRoom(user)
        room?.startStreaming(mInfo)
        return Result(true,"Start Streaming Success!",null)
    }

    //关闭放映
    @PostMapping("/close_streaming")
    fun closeStreaming(@RequestBody user:User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        room?.closeStreaming()
        return Result(true,"Close Streaming Success!",null)
    }

    //获取媒体信息
    @PostMapping("/get_media_info")
    fun getMediaInfo(@RequestBody user:User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        val mInfo=room?.mediaInfo
        if(mInfo!=null) return Result(true,"Get MediaInfo Success!",Gson().toJson(mInfo))
        else return Result(false,"Get MediaInfo Failed!",null)
    }

    //检查是否正在放映
    @PostMapping("/check_video_on")
    fun checkVideoOn(@RequestBody user:User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        if(room==null){
            return Result(false,"No Binder!",null)
        }else return Result(true,"CheckCheck!",room.videoOn)
    }

    //上传控制命令
    @PostMapping("/update_progress")
    fun updateProgress(@RequestBody user:User?,@RequestParam("p") progress:Long):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        return if(room?.updateProgress(user,progress)!!){
            Result(true,"Update Progress Success!",progress)
        }else Result(false,"Update Progress Failed!",progress)
    }

    //获取控制命令
    @PostMapping("/get_progress")
    fun getProgress(@RequestBody user:User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        return Result(true,"Get Progress Success!",room?.getProgress(user))
    }

    //更新实时播放进度
    @PostMapping("/update_playing_timestamp")
    fun updatePlayingTimeStamp(@RequestBody user:User?,@RequestParam("timeStamp") v:Long):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        if(room?.updatePlayingTimeStamp(v)!!){
            return Result(true,"Update Playing Timestamp Success!",v)
        }else{
            return Result(false,"Update Playing Timestamp Failed!",v)
        }
    }

    //获取实时播放进度
    @PostMapping("/get_playing_timestamp")
    fun getPlayingTimeStamp(@RequestBody user:User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        return Result(true,"Get Playing Timestamp Success!",room?.getPlayingTimeStamp())
    }

    //设置实时cacheTime
    @PostMapping("/set_cache_time")
    fun setCacheTime(@RequestBody user: User?,@RequestParam("v") v:Long):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        if(room?.setCacheTime(v)!!){
            return Result(true,"Set Cache Time Success!",v)
        }else{
            return Result(false,"Set Cache Time Failed!",v)
        }
    }

    //获取实时cacheTime
    @PostMapping("/get_cache_time")
    fun getCacheTime(@RequestBody user: User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room=RoomStore.getRoom(user)
        return Result(true,"Get Cache Time Success!",room?.getCacheTime())
    }
}