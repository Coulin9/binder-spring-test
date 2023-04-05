package com.zhaoxinyu.binderspringtest.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import net.sf.jsqlparser.expression.DateTimeLiteralExpression
import java.util.*
import kotlin.collections.HashMap

object Utils {

    //token密钥： ZZZZzxyyzx90
    const val SECRET="ZZZZzxyyzx90"

    //默认的token过期时间为10小时
    const val TOKEN_VALID_TIME=60*60*10000

    //依据用户账户与密码生成Token，有效期时间为秒
    fun generateToken(account:String,password:String,validTime:Int= TOKEN_VALID_TIME):String{
        return JWT.create()
            .withClaim("account",account)
            .withClaim("password",password)
            .withExpiresAt(Date(Date().time+validTime*1000))
            .sign(Algorithm.HMAC256(SECRET))
    }
}