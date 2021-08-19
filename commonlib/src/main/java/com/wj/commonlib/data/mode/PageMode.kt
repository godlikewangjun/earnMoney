package com.wj.commonlib.data.mode


/**
 * 分页的mode
 * @author dchain
 * @version 1.0
 * @date 2019/8/2
 */
data class PageMode<E>(
    val isEnd: Boolean,
    var list: ArrayList<E>,
    val page: Int,
    val pageCount: Int,
    val total: Int
)
