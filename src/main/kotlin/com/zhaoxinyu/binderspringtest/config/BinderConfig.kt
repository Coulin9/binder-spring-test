package com.zhaoxinyu.binderspringtest.config

import com.zhaoxinyu.binderspringtest.utils.LoginStateInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class BinderConfig:WebMvcConfigurer {

    @Autowired
    lateinit var loginStateInterceptor:LoginStateInterceptor
    override fun addInterceptors(registry: InterceptorRegistry) {
         registry.addInterceptor(loginStateInterceptor).addPathPatterns("/**")
            .excludePathPatterns("/login","/register","/testBind","/testLoginState","/update_playing_timestamp",
            "/get_progress","/check_video_on","/set_cache_time","/close_streaming","/start_streaming","/get_media_info",
                "/update_progress","/get_playing_timestamp","/get_cache_time","/get_new_message","/getUserIcon",
                "/getInfo","/updateInfo")
    }
}