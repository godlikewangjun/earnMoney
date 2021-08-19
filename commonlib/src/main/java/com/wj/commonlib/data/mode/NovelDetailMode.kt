package com.wj.commonlib.data.mode

/**
 * 小说内容
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
data class NovelDetailMode(
    val code: Int,
    val content: List<String>,
    val message: String,
    val num: String
)