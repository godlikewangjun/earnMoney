package com.wj.commonlib.data.mode

import android.os.Parcel
import android.os.Parcelable

/**
 * 视频搜索的详情
 * @author dchain
 * @version 1.0
 * @date 2019/10/17
 */
data class VipMovieDetailMode(
    val code: Int,
    val `data`: VipMovieDetailInfo,
    val list: ArrayList<VipMovieDetailItem>,
    val message: String
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readParcelable<VipMovieDetailInfo>(VipMovieDetailInfo::class.java.classLoader)!!,
        source.createTypedArrayList(VipMovieDetailItem.CREATOR)!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(code)
        writeParcelable(data, 0)
        writeTypedList(list)
        writeString(message)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VipMovieDetailMode> =
            object : Parcelable.Creator<VipMovieDetailMode> {
                override fun createFromParcel(source: Parcel): VipMovieDetailMode =
                    VipMovieDetailMode(source)

                override fun newArray(size: Int): Array<VipMovieDetailMode?> = arrayOfNulls(size)
            }
    }
}

data class VipMovieDetailInfo(
    var Language: String?,
    val Release: String?,
    val cover: String,
    var director: String?,
    val genre: String?,
    var introduce: String?,
    var performer: String?,
    val region: String?,
    var title: String,
    val time: String?
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString()!!,
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()!!,
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(Language)
        writeString(Release)
        writeString(cover)
        writeString(director)
        writeString(genre)
        writeString(introduce)
        writeString(performer)
        writeString(region)
        writeString(title)
        writeString(time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VipMovieDetailInfo> =
            object : Parcelable.Creator<VipMovieDetailInfo> {
                override fun createFromParcel(source: Parcel): VipMovieDetailInfo =
                    VipMovieDetailInfo(source)

                override fun newArray(size: Int): Array<VipMovieDetailInfo?> = arrayOfNulls(size)
            }
    }
}

data class VipMovieDetailItem(
    val download: String,
    val m3u8url: String,
    val num: String,
    val onlineurl: String
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(download)
        writeString(m3u8url)
        writeString(num)
        writeString(onlineurl)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VipMovieDetailItem> =
            object : Parcelable.Creator<VipMovieDetailItem> {
                override fun createFromParcel(source: Parcel): VipMovieDetailItem =
                    VipMovieDetailItem(source)

                override fun newArray(size: Int): Array<VipMovieDetailItem?> = arrayOfNulls(size)
            }
    }
}

data class VipParsMovieMode(
    val code: Int,
    val `data`: VipMovieDetailDataMode,
    val msg: String
)

data class VipMovieDetailDataMode(
    val count: Int,
    val `data`: ArrayList<VipMovieItemMode>
)

data class VipMovieItemMode(
    val chapterUrl: String,
    val creationTime: String,
    val id: Int,
    val title: String,
    val videoId: String,
    val videoVariableId: Int
)