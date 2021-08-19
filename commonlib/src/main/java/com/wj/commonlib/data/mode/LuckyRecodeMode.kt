package com.wj.commonlib.data.mode

/**
 * 抽奖记录表
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
data class LuckyRecodeMode(
    val createtime: String,
    val luckyTitle: String,
    val points: Int
)