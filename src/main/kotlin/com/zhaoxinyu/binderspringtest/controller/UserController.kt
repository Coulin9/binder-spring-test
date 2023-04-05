package com.zhaoxinyu.binderspringtest.controller

import com.auth0.jwt.JWT
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.google.gson.Gson
import com.zhaoxinyu.binderspringtest.bean.Result
import com.zhaoxinyu.binderspringtest.bean.User
import com.zhaoxinyu.binderspringtest.service.UserService
import com.zhaoxinyu.binderspringtest.utils.Utils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.*
import kotlin.Exception
import kotlin.concurrent.thread


@RestController
class UserController {
    companion object{
        const val IconPath="/Users/zhaoxinyu/userIcons/"
    }

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

    //登陆
    @GetMapping("/login")
    fun login(@RequestParam("account") account:String, @RequestParam("password") password:String):Result{
        //依据account去数据库中查询用户，然后比对密码。
        val users=userService.list(QueryWrapper<User>().allEq(mapOf("account" to account)))
        //登陆成功，为用户生成一个token并返回
        if(users.size>0&&users[0].password==password){
            val token=Utils.generateToken(account,password)
            val ops=redisTemplate.opsForValue()
            //将生成的token存入redis中
            ops.set("${account}:token",token)
            //为了让安卓客户端可以识别，这里使用Gson转换返回给客户端的user对象
            return Result(true,"Login success!", mapOf("token" to token,"loginUser" to Gson().toJson(users[0])))
        }else if(users.size==0){
            return Result(false,"Invalid User!")
        }else return Result(false,"Login failed!")
    }

    //登出
    @GetMapping("/logout")
    fun logout(request: HttpServletRequest,response: HttpServletResponse):Result{
        //登出就是去掉当前用户在redis缓存中的token
        val token=request.getHeader("token")
        val decoder=JWT.decode(token)
        val account=decoder.getClaim("account").asString()
        val ops=redisTemplate.opsForValue()
        try{
            ops.getAndDelete("${account}:token")
            //登出的响应不应该带有token
            //response.setHeader("token","")
            return Result(true,"Logout success!",null)
        }catch (e:Exception){
            return Result(false,"Logout failed! ${e.message}")
        }
    }

    //注册
    @PostMapping("/register")
    fun register(@RequestBody user: User?):Result{
        //强制account为用户电子邮件地址！
        if(user!=null){
            try {
                userService.save(user)
                //默认用户名为bind_user_id
                userService.update(UpdateWrapper<User>().allEq(mapOf("account" to user.account)).set("user_name","bind_user_${user.id}"))
                return Result(true,"Register success!",user)
            }catch (e:Exception){
                return Result(false,"Register failed! ${e.message}")
            }
        }else return Result(false,"Register failed!")
    }

    //注销
    @PostMapping("/unRegister")
    fun unRegister(@RequestBody user: User?,request: HttpServletRequest):Result{
        try{
            //注销不仅仅要将用户信息从数据库中移除，还要移除他的token信息以及绑定信息
            if(unbind(request).success&&userService.removeById(user)){
                //移除token
                val ops=redisTemplate.opsForValue()
                ops.getAndDelete("${user?.account}:token")
                return Result(true,"Unregister success!")
            }else return Result(false,"Unregister failed! No target user!")
        }catch (e:Exception){
            return Result(false,"Unregister failed! ${e.message}")
        }
    }

    //更新用户信息(除了头像)
    @PostMapping("/updateUser")
    fun updateUser(@RequestBody user: User):Result{
        try{
            //将新的用户信息存入数据库，如果成功那么就返回更新后的用户
            if(userService.update(user,UpdateWrapper<User>().allEq(mapOf("id" to user.id)))){
                return Result(true,"Update User success!",Gson().toJson(user))
            }else{
                return Result(false,"Update User failed!",null)
            }
        }catch (e:Exception){
            return Result(false,"Update User failed! ${e.message}")
        }
    }

    //更新用户头像
    @PostMapping("/updateIcon")
    fun updateIcon(@RequestParam("file") file:MultipartFile, request: HttpServletRequest):Result{
        if(!file.isEmpty){
            val decoder=JWT.decode(request.getHeader("token"))
            val account=decoder.getClaim("account").asString()
            //val tAccount=account.substring(1,account.length-1)
            val storedPath=IconPath+"${account}_"+file.originalFilename
            try{
                file.transferTo(File(storedPath))
                if( userService.update(UpdateWrapper<User>().set("icon_path",storedPath).eq("account",account))){
                    return Result(true,"Update Icon Success!",storedPath)
                }else return Result(false,"Update Icon Failed! SQL ERROR!")
            }catch (e:Exception){
                return Result(false,"Update Icon Failed! ${e.message}",null)
            }
        }else{
            return Result(false,"Update Icon Failed! Empty File!",null)
        }
    }

    //获取用户头像
    @GetMapping("/getUserIcon")
    fun getUserIcon(@RequestParam("id") id:Int, response: HttpServletResponse){
        val user=userService.getOne(QueryWrapper<User>().eq("id",id))
        if(user.iconPath!=null){
            val path=user.iconPath
            val file=File(path)
            response.reset()
            response.setContentType("application/octet-stream;charset=utf-8")
            response.setHeader(
                "Content-disposition",
                "attachment; filename=Res.${path.substringAfterLast('.')}")
            try{
                    val bis = BufferedInputStream(file.inputStream())
                     val bos = BufferedOutputStream(response.getOutputStream())
                    val buff = ByteArray(1024)
                    var len = 0
                    len = bis.read(buff)
                    while (len > 0) {
                        bos.write(buff, 0, len)
                        len=bis.read(buff)
                    }
                bis.close()
                bos.close()
                }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }


    //进入待绑定状态
    @GetMapping("/enterBindingState")
    fun enterBindingState(request: HttpServletRequest):Result{
        //从token中获取account
        val token=request.getHeader("token")
        val decoder=JWT.decode(token)
        val account=decoder.getClaim("account").asString()
        //截取token倒数四位作为绑定码
        val bindCode=token.substring(token.length-4,token.length)
        val ops=redisTemplate.opsForValue()
        try{
            //存绑定码之前先看redis中是否有该账号的绑定码，有的话需要先删除。
            val oldCode=ops.getAndDelete("${account}")
            if(oldCode!=null) ops.getAndDelete("${oldCode}")
            ops.set("${bindCode}","${account}")
            ops.set("${account}","${bindCode}")
            //进入待绑定状态成功，服务器将绑定码返回给客户端
            return Result(true,"Enter bindingState success!",bindCode)
        }catch (e:Exception){
            return Result(false,"Enter bindingState failed! ${e.message}")
        }
    }

    //退出待绑定状态
    @GetMapping("/exitBindingState")
    fun exitBindingState(@RequestParam("bindCode") bindCode:String):Result{
        //退出待绑定状态需要带上自己的绑定码才行.
        val ops=redisTemplate.opsForValue()
        try{
            val account=ops.getAndDelete("${bindCode}")
            ops.getAndDelete("${account}")
            return Result(true,"Exit success!")
        }catch (e:Exception){
            return Result(false,"Exit failed! ${e.message}")
        }
    }

    //绑定请求
    @GetMapping("/bind")
    fun bind(@RequestParam("bindCode") bindCode: String,request: HttpServletRequest):Result{
        //获取自己的account
        val token=request.getHeader("token")
        val decoder=JWT.decode(token)
        val myAccount=decoder.getClaim("account").asString()

        val ops=redisTemplate.opsForValue()
        val taAccount=ops[bindCode]
        //不能自己与自己绑定
        if(taAccount!=null&&myAccount!=taAccount){
            //更新双方的bind_id
            try{
                val me=userService.list(QueryWrapper<User>().allEq(mapOf("account" to myAccount)))[0]
                val ta=userService.list(QueryWrapper<User>().allEq(mapOf("account" to taAccount)))[0]
                userService.update(UpdateWrapper<User>().set("binder_id",me.id).allEq(mapOf("id" to ta.id)))
                userService.update(UpdateWrapper<User>().set("binder_id",ta.id).allEq(mapOf("id" to me.id)))
                return Result(true,"Bind success!",ta.id)
            }catch (e:Exception){
                return Result(false,"Bind failed! ${e.message}")
            }
        }else return Result(false,"Bind failed!")
    }

    //解绑请求
    @GetMapping("/unBind")
    fun unBind(request: HttpServletRequest):Result=unbind(request)

    fun unbind(request: HttpServletRequest):Result{
        //获取自己的account
        val token=request.getHeader("token")
        val decoder=JWT.decode(token)
        val myAccount=decoder.getClaim("account").asString()

        val me=let {
            val mes=userService.list(QueryWrapper<User>().allEq(mapOf("account" to myAccount)))
            if(mes.size>0) mes[0]
            else null
        }
        //必须绑定了才可以解绑
        if(me?.binderId != null){
            try{
                //解绑
                val ta=userService.list(QueryWrapper<User>().allEq(mapOf("id" to me.binderId)))[0]
                userService.update(UpdateWrapper<User>().set("binder_id",null).allEq(mapOf("id" to me.id)))
                userService.update(UpdateWrapper<User>().set("binder_id",null).allEq(mapOf("id" to ta.id)))
                return Result(true,"unBind success!")
            }catch (e:Exception){
                return Result(false,"unBind failed! ${e.message}")
            }
        }else return Result(true,"It's already unbind!")
    }

    //检查绑定状态
    @GetMapping("/testBind")
    fun testBind(request: HttpServletRequest):Result{
        //获取自己的account
        val token=request.getHeader("token")
        val decoder=JWT.decode(token)
        val account=decoder.getClaim("account").asString()

        //依据account查询user
        val users=userService.list(QueryWrapper<User>().allEq(mapOf("account" to account)))
        if(users!=null){
            val user=users[0]
            if(user?.binderId!=null){
                return Result(true,"Already bind!",user.binderId)
            }else return Result(true,"Not bind!")
        }else return Result(false ,"Invalid user!")
    }

    //检查登陆状态
    @GetMapping("/testLoginState")
    fun testLoginState(request: HttpServletRequest):Result{
        val token=request.getHeader("token")
        val ops=redisTemplate.opsForValue()
        //如果token不为空
        if(token!=null&&token!=""){
            //检查token是否有效
            //从token中解析出account
            val decoder=JWT.decode(token)
            val account=decoder.getClaim("account").asString()
            //获取本地存储的token
            val rToken=ops.get("${account}:token")
            val expireDate=decoder.expiresAt
            val now= Date()
            //如果token相同且未过期，表明登陆有效
            if(rToken==token&&now<expireDate){
                return Result(true,"Valid login")
            }else return Result(false,"Invalid token!")
        }else return Result(false,"Invalid token!") 
    }

    @GetMapping("/getBinder")
    fun getBinder(request: HttpServletRequest):Result{
        val decoder=JWT.decode(request.getHeader("token"))
        val account=decoder.getClaim("account").asString()
        try {
            val user=userService.getOne(QueryWrapper<User>().eq("account",account))
            val binder=userService.getOne(QueryWrapper<User>().eq("id",user.binderId))
            return Result(true,"Get Binder Success!",Gson().toJson(binder))
        }catch (e:Exception){
            e.printStackTrace()
            return Result(false,"Get Binder ERROR!")
        }
    }
}