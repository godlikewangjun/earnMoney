package com.wj.commonlib.data.mode

/**
 * 升级
 * @author Admin
 * @version 1.0
 * @date 2018/6/20
 */

 data class UpDataMode(
    val appupdateid: Int,
    val channel: String,
    val creatime: String,
    val describe: String,
    val key: String,
    val platform: Int,
    val type: Int,
    val url: String,
    val userid: Int,
    val version: Int,
    val versionname: String,
    var isforce: Int
)