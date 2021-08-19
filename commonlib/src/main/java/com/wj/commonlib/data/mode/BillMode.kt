package com.wj.commonlib.data.mode

/**
 * 账单
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
data class BillMode(
    val bill_chang_points: Double,
    val bill_creatime: String,
    val bill_desc: String,
    val bill_ad_icon: String,
    val bill_money: Double
)