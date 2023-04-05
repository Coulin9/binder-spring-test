package com.zhaoxinyu.binderspringtest

import com.auth0.jwt.JWT
import com.google.gson.Gson
import com.zhaoxinyu.binderspringtest.bean.Diary
import com.zhaoxinyu.binderspringtest.bean.MediaInfo
import com.zhaoxinyu.binderspringtest.bean.User
import com.zhaoxinyu.binderspringtest.mapper.UserMapper
import com.zhaoxinyu.binderspringtest.service.UserService
import com.zhaoxinyu.binderspringtest.utils.Utils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

@SpringBootTest
class BinderSpringTestApplicationTests {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var redisTemplate:StringRedisTemplate
    @Test
    fun contextLoads() {
        val users=userService.list()
        println(users)
    }

    @Test
    fun redisTest(){
        val opts=redisTemplate.opsForValue()
        opts.set("test","Fuck!")
        println(opts["test"])
    }

    @Test
    fun insertUser(){
        userService.save(User(2,"Jack","JJ","1234567","male"
        , Date(),"",null))
    }

    @Test
    fun tokenTest(){
        val token=Utils.generateToken("123","12345678",)
        println(JWT.decode(token).expiresAt)
    }

    @Test
    fun mInfoTest(){
        val mInfo=MediaInfo()
        val map=HashMap<String,Any?>()
        map["user"]=1234567
        map["mInfo"]=mInfo
        println(Gson().toJson(map))
        //val heap=PriorityQueue<Int>()
    }

    @Test
    fun diaryTest(){
        val r=Random(435134).nextInt(0,12)
        val diary=Diary(0,1,Date(),"11111")
        println(Gson().toJson(diary))

        val l= Array<Pair<Int,ArrayDeque<Int>>?>(10){
            null
        }

        val sb=StringBuilder()
        while(sb[sb.length-1]==','||sb[sb.length-1]=='n'){
            sb.deleteAt(sb.length-1)
        }
    }

}
