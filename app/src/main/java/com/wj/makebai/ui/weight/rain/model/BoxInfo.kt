package com.wj.makebai.ui.weight.rain.model

/**
 * Created on 2018/10/3.
 * 红包
 *
 * @author ice
 */
class BoxInfo {
    //红包ID（拿这个去问服务器是否中奖）
    var awardId = 0
    //红包校验
    var voucher: String? = null
    //红包类型 0是空 1是有礼物
    var type =0
}