package com.wj.commonlib.data.mode

/**
 * 广告
 * @author dchain
 * @version 1.0
 * @date 2019/4/12
 */
data class AdMode(
    val adid: Int,
    val adkey: String,
    val ads_link: String,
    val adtype: String,
    val creatime: String,
    val describe: String,
    val img_url: String,
    val state: Int
)