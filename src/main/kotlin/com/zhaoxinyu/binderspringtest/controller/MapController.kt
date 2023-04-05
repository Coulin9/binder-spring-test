package com.zhaoxinyu.binderspringtest.controller

import com.zhaoxinyu.binderspringtest.bean.Result
import com.zhaoxinyu.binderspringtest.bean.RoomStore
import com.zhaoxinyu.binderspringtest.bean.User
import org.springframework.web.bind.annotation.*

@RestController
class MapController {

    @PostMapping("/getInfo")
    fun getInfo(@RequestBody user:User?):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room= RoomStore.getRoom(user)
        val res=room?.getInfos(user.id)
        if(res!=null&&res!="") return Result(true,"Get Info Success!",res)
        else return Result(false,"Get Info Failed!")
    }

    @PostMapping("/updateInfo")
    fun updateInfo(@RequestBody user:User?,@RequestParam("info") info:String):Result{
        if(user==null) return Result(false,"Null User!",null)
        val room= RoomStore.getRoom(user)
        if(room!=null&&room.updateInfos(user.id,info)){
            return Result(true,"Update Info Success!")
        }else return Result(false,"Update Info Failed!")
    }
}