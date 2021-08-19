package com.wj.commonlib.data.mode

/**
 * 绑定的支付信息
 * @author Administrator
 * @version 1.0
 * @date 2019/11/28
 */
data class PaymentInfoMode(
    val isFlow: Boolean,
    val payment: WechatBind
)
data class WechatBind(
    val phone: String,
    val type: Int,
    val usernum: String
)