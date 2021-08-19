package com.wj.commonlib.data.mode

/**
 * 首页功能的分类
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
data class HomeToolsMode(
    val creatime: String,
    val describe: String,
    val img_url: String,
    val points: Int,
    val state: Int,
    val toolid: Int,
    val tools_link: String,
    val type: Int
)