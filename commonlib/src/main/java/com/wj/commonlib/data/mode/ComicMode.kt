package com.wj.commonlib.data.mode

import com.wj.commonlib.data.mode.dz.DataX

/**
 * 漫画实例
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
data class ComicMode(
    val code: Int,
    val `data`: ComicDataMode,
    val msg: String
)

data class ComicDataMode(
    val count: Int,
    val `data`: ArrayList<ComicItem>,
    val from: Int,
    val size: Int
)

data class ComicItem(
    val author: String,
    val cartoonId: String,
    val cartoonType: String,
    val chapterId: String,
    val cartoonVariableId: Int,
    val cover: String,
    val descs: String,
    val id: Int,
    val title: String,
    val updateTime: String
)