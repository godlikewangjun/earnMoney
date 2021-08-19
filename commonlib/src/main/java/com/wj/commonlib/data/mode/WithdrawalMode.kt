package com.wj.commonlib.data.mode

/**
 * 提现申请的流水
 * @author Administrator
 * @version 1.0
 * @date 2019/11/28
 */
data class WithdrawalMode(
    val balance: Int,
    val creatime: String,
    val withdrawal_name: String,
    val idwithdrawal: Int,
    val idwithdrawal_type: Int,
    val issetstate: Int,
    val spendingtotal: Int,
    val userid: Int,
    val withdrawal_money: Double,
    val withdrawal_point: Int,
    val withdrawal_state: Int,
    val describe:String
)