package com.wj.commonlib.data.mode

import com.wj.commonlib.data.mode.dz.DataX
import java.io.Serializable

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/1/9
 */
data class NovelPageMode(
    val code: Int,
    val `data`: NovelPageDataMode,
    val msg: String
)

data class NovelPageDataMode(
    val count: Int,
    val `data`: ArrayList<NovelPageItemMode>
)

data class NovelPageItemMode(
    val chapterId: String,
    val creationTime: String,
    val fictionId: String,
    val fictionVariableId: Int,
    val id: Int,
    val title: String
)