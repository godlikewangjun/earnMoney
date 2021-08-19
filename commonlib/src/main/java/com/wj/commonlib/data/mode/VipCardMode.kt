package com.wj.commonlib.data.mode

/**
 *
 * @author admin
 * @version 1.0
 * @date 2020/12/21
 */
data class VipCardMode(
    val desc: String,
    val discountNum: Double,
    val discountToTime: String,
    val payMoney: Double,
    val toTime: Int,
    val vipCardId: Int
)