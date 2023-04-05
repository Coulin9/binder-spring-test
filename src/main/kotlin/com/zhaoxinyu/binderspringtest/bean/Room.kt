package com.zhaoxinyu.binderspringtest.bean

import java.util.ArrayDeque

class Room {
    companion object{
        const val VIDEO_PAUSE=-1L
        //获取进度失败的返回值
        const val PRO_ERROR=-2L
        const val VIDEO_RESUME=-3L
    }
    //保存房间内的两名用户的id
    lateinit var userPair:Pair<Int,Int>

    //双方的实时信息(json字符串的形式保存)
    val infos= Array(1){""} to  Array(1){""}

    //实时消息队列
    val messageQs=ArrayDeque<String>() to ArrayDeque<String>()

    //正在播放的媒体信息
    var mediaInfo:MediaInfo?=null

    //是否正在放映的标志位
    val videoOn:Boolean get() = _videoOn
    private var _videoOn=false

    //双方的控制命令
    private val progressInfos= LongArray(1){
        PRO_ERROR
    } to LongArray(1){
        PRO_ERROR
    }

    //实时播放进度
    private var playingTimeStamp=0L

    //实时缓冲时间（主用户推流进度-当前播放进度）
    private var cacheTime:Long=0L

    private constructor(){
    }
    constructor(userPair:Pair<Int,Int>):this(){
        this.userPair=userPair
    }

    //向自己的消息队列插入一条消息
    fun sendMessage(user:User,msg:String):Boolean= synchronized(this){
        var q:ArrayDeque<String>?=null
        //先依据userId判断需要插入到哪一个队列中
        q = if(user.id==userPair.first){
            messageQs.first
        }else if(user.id==userPair.second){
            messageQs.second
        }else{
            return false
        }
        q.offer(msg)
        return true
    }

    //获取对方队首的一条消息
    fun getNewMessage(user:User):String?= synchronized(this){
        var q:ArrayDeque<String>?=null
        //先依据userId判断需获取哪一个队列
        q = if(user.id==userPair.first){
            messageQs.second
        }else if(user.id==userPair.second){
            messageQs.first
        }else{
            return null
        }
        return q.poll()
    }

    //开启放映
    fun startStreaming(mInfo:MediaInfo)= synchronized(this){
        _videoOn=true
        mediaInfo=mInfo
    }

    //结束放映
    fun closeStreaming()= synchronized(this){
        _videoOn=false
        progressInfos.first[0]= PRO_ERROR
        progressInfos.second[0]= PRO_ERROR
        cacheTime=0L
        playingTimeStamp=0L
        mediaInfo=null
    }

    //检查是否正在放映
    fun isVideoOn():Boolean= synchronized(this){
        return videoOn
    }

    //更新控制命令
    fun updateProgress(user: User,p:Long):Boolean= synchronized(this){
        //将命令同步插入双方的命令槽中
        //println("跟新控制命令：${p}")
        if(videoOn){
            progressInfos.second[0]=p
            progressInfos.first[0]=p
            return@synchronized true
        }else{
            return@synchronized false
        }
    }

    //获取最新的控制命令
    fun getProgress(user: User):Long= synchronized(this){
        val progressInfo=if(user.id==userPair.first){
            progressInfos.second
        }else if(user.id==userPair.second){
            progressInfos.first
        }else{
            return@synchronized PRO_ERROR
        }
        if(videoOn) {
            val res=progressInfo[0]
            //println("获取控制命令：${res}")
            progressInfo[0]= PRO_ERROR
            return@synchronized res
        }
        else return@synchronized PRO_ERROR
    }

    //更新实时播放进度
    fun updatePlayingTimeStamp(v:Long):Boolean= synchronized(this){
        if(videoOn){
            playingTimeStamp=v
            //println("更新播放时间：${playingTimeStamp}")
            return true
        }else return false
    }

    //获取实时播放进度
    fun getPlayingTimeStamp():Long= synchronized(this){
        //println("获取播放进度：${playingTimeStamp}")
        return@synchronized playingTimeStamp
    }

    //更新实时cacheTime
    fun setCacheTime(v:Long):Boolean= synchronized(this){
        if(videoOn){
            cacheTime=v
            //println("更新cacheTime：${cacheTime}")
            return true
        }else return false
    }

    //获取实时cacheTime
    fun getCacheTime():Long= synchronized(this){
        //println("获取cacheTime：${cacheTime}")
        return@synchronized cacheTime
    }

    //获取对方的实时信息
    fun getInfos(userId:Int?):String?= synchronized(this){
        return@synchronized if(userId==userPair.first) infos.second[0]
        else if(userId==userPair.second) infos.first[0]
        else null
    }

    //更新自己的实时信息
    fun updateInfos(userId: Int?,info:String):Boolean= synchronized(this){
        if(userId==userPair.first) {
            infos.first[0]=info
            return@synchronized true
        }
        else if(userId==userPair.second){
            infos.second[0]=info
            return@synchronized  true
        }else return@synchronized  false
    }
}