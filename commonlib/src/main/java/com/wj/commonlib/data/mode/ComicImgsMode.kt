package com.wj.commonlib.data.mode


/**
 * 漫画图片
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
data class ComicImgsMode(
    val code: Int,
    val `data`: ComicImgDataMode,
    val msg: String
)

data class ComicImgDataMode(
    val count: Int,
    val `data`: ComicImgItemMode
)

data class  ComicImgItemMode(
    val cartoonId: String,
    val cartoonVariableId: Int,
    val chapterId: String,
    val content: ArrayList<String>,
    val id: Int
)