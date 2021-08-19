package com.wj.commonlib.data.mode

/**
 * 任务配置信息
 * @author Administrator
 * @version 1.0
 * @date 2019/12/18
 */
data class TaskConfigMode(
    val artAllShareCount: Int,
    val artShareCount: Int,
    val isSign: Boolean,
    val luckCount: Int,
    val shareAllCount: Int,
    val shareCount: Int,
    val videoCount: Int,
    val videoAllCount: Int
)