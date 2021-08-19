package com.wj.commonlib.data.mode.uc

/**
 * 城市code 请求本地的新闻
 * @author dchain
 * @version 1.0
 * @date 2019/10/24
 */
data class UcCityCodes(
    val `data`: Data,
    val extension: Extension,
    val message: String,
    val result: Result,
    val status: Int
)

data class Data(
    val cities: List<City>
)

data class City(
    val code: String,
    val letter: String,
    val name: String
)