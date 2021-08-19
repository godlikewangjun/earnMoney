package com.wj.commonlib.data.mode

import com.wj.commonlib.data.mode.dz.DataX

/**
 * 小说内容
 * @author dchain
 * @version 1.0
 * @date 2019/9/23
 */
data class NovelContentMode(
    val code: Int,
    val `data`: NovelContentItmMode,
    val msg: String
)

data class NovelContentItmMode(
    val count: Int,
    val `data`: NovelContentListMode
)

data class NovelContentListMode(
    val chapterId: String,
    val content: List<String>,
    val creationTime: Long,
    val fictionId: String,
    val fictionVariableId: Int,
    val id: Int
)