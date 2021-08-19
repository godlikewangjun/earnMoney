package com.wj.commonlib.data.mode

/**
 * vip影视的第三方接口
 * @author dchain
 * @version 1.0
 * @date 2019/10/16
 */
data class VipMovieMode(
    val code: Int,
    val `data`: VipMovieDataMode,
    val msg: String
)

data class VipMovieItem(
    val author: String,
    val cover: String,
    val creationTime: String,
    val descs: String,
    val id: Int,
    val title: String,
    val updateTime: String,
    val videoId: String,
    val videoType: String,
    val videoVariableId: Int
)

data class VipMovieDataMode(
    val count: Int,
    val `data`: ArrayList<VipMovieItem>,
    val from: Int,
    val size: Int
)