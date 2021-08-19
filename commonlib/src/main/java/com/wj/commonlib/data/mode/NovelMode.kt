package com.wj.commonlib.data.mode

/**
 * 小说
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
data class NovelMode(
    val code: Int,
    val `data`: Data,
    val msg: String
)

data class  NovelItem(
    val author: String,
    val cover: String,
    val descs: String,
    val fictionId: String,
    val fictionType: String,
    val fictionVariableId: Int,
    val id: Int,
    val title: String,
    val updateTime: String
)

data class Data(
    val count: Int,
    val `data`: ArrayList<NovelItem>,
    val from: Int,
    val size: Int
)