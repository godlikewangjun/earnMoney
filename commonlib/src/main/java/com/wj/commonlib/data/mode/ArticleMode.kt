package com.wj.commonlib.data.mode

import java.io.Serializable

/**
 * 文章
 * @author dchain
 * @version 1.0
 * @date 2019/8/2
 */
data class ArticleMode(
    val articlecommentcount: Int,
    val articlecontent: String,
    var like_count: Int,
    var read_count: Int,
    val articlecreattime: String,
    val articleid: Int,
    val articlename: String,
    val articlepoints: Int,
    val articletimes: String,
    val articletotal: Int,
    val author: Int,
    val creatime: String,
    val digest: String,
    val imageurls: String,
    val level: Int,
    val source: String,
    val tag: String,
    val key: String,
    val type: Int,
    val usericon: String,
    val username: String,
    val videourl: String,
    var isRead: Boolean
):Serializable