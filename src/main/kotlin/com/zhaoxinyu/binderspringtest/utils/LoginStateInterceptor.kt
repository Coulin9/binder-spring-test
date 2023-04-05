package com.zhaoxinyu.binderspringtest.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import com.zhaoxinyu.binderspringtest.bean.Result
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

@Component
class LoginStateInterceptor:HandlerInterceptor {

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token=request.getHeader("token")
        val ops=redisTemplate.opsForValue()
        //如果请求带有token并且该token存在且有效的话
        if(token!=null&&token!=""){
            //解析token获得用户的account，密码以及token过期时间
            val decoder=JWT.decode(token)
            val account= decoder.getClaim("account").asString()
            val password=decoder.getClaim("password").asString()
            //从redis获取该用户已存储的token，并获取过期日期
            val rToken=ops["${account}:token"]
            val expireDate= decoder.expiresAt
            val now= Date()
            //如果请求的token与redis中存储的token相同并且token未过期就放行请求
            if(token==rToken&&now<=expireDate){
                //刷新token并放行请求
                val newToken=Utils.generateToken(account,password,Utils.TOKEN_VALID_TIME)
                ops.set("${account}:token",newToken)
                //将新的token放到响应头中。
                response.addHeader("token",newToken)
                return true
            }
        }
        //否则，拦截并返回一个Json。
        val res=Result(false,"Invalid token!")
        response.writer.print(Gson().toJson(res))
        response.flushBuffer()
        return false
    }
}