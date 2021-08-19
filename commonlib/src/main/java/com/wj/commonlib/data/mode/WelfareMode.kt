package com.wj.commonlib.data.mode

/**
 * 福利列表
 * @author Administrator
 * @version 1.0
 * @date 2019/11/15
 */
data class WelfareMode(
    val conditions: Int,
    val createtime: String,
    val describe: String,
    val detail: String,
    val image: String,
    val title: String,
    val toDate: Any,
    val type: Int,
    val value: Double,
    val welfareid: Int,
    var hasGet:Boolean=false,
    var userd:Boolean=false
)