package com.wj.commonlib.data.mode

import android.os.Parcel
import android.os.Parcelable
import com.wj.commonlib.data.mode.dz.DataX

/**
 * 漫画详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
data class ComicDetailMode(
    val code: Int,
    val `data`: ComicDetailData,
    val msg: String
)

data class ComicDetailData(
    val count: Int,
    val `data`: ArrayList<NovelPageItemMode>
)
