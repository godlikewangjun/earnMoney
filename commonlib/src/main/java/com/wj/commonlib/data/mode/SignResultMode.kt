package com.wj.commonlib.data.mode

/**
 * 签到返回的
 * @author Administrator
 * @version 1.0
 * @date 2019/11/14
 */
data class SignResultMode(
    val points: Int,
    val sign_time: String,
    val sing_count: Int,
    val userBeforePoints: Int,
    val userPoints: Int,
    val userid: Int
)