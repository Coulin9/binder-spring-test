package com.zhaoxinyu.binderspringtest.bean

object RoomStore {
    private val map=HashMap<String,Room>()

    private fun get(key:String):Room?= synchronized(this){
        return@synchronized map[key]
    }

    private fun add(key: String,value: Room)= synchronized(this){
        map[key]=value
    }

    //依据用户获取id，不存在就创建
    fun getRoom(user: User):Room?= synchronized(this){
        val uId=user.id
        val bId=user.binderId
        if(bId==null){
            return null
        }
        val key="${if(uId!!>bId) bId else uId}:${if(uId>bId) uId else bId}"
        var res:Room?=null
        res=get(key)
        if(res==null){
            //不存在，依据userid创建一个Room对象
            val userPair=(if(uId>bId) bId else uId) to (if(uId>bId) uId else bId)
            val room=Room(userPair)
            add(key,room)
            return room
        }else return res
    }
}