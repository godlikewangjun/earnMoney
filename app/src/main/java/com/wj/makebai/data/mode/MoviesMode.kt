package com.wj.makebai.data.mode

import android.os.Parcel
import android.os.Parcelable

/**
 * 影视信息
 * @author dchain
 * @version 1.0
 * @date 2019/9/10
 */
data class MoviesMode(
    val actors: String,
    val area: String,
    val director: String,
    val image: String,
    val introduction: String,
    val key: String,
    val language: String,
    val pushDate: String,
    val state: String,
    val title: String,
    val type: String,
    val detail: String
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(actors)
        writeString(area)
        writeString(director)
        writeString(image)
        writeString(introduction)
        writeString(key)
        writeString(language)
        writeString(pushDate)
        writeString(state)
        writeString(title)
        writeString(type)
        writeString(detail)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MoviesMode> = object : Parcelable.Creator<MoviesMode> {
            override fun createFromParcel(source: Parcel): MoviesMode = MoviesMode(source)
            override fun newArray(size: Int): Array<MoviesMode?> = arrayOfNulls(size)
        }
    }
}