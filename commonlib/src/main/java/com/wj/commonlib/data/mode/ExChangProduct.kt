package com.wj.commonlib.data.mode

/**
 * 提现的产品
 * @author Administrator
 * @version 1.0
 * @date 2019/11/16
 */
data class ExChangProduct(
    val idwithdrawal_type: Int,
    val withdrawal_name: String,
    val withdrawal_point: Int,
    val withdrawal_sale: Double
)