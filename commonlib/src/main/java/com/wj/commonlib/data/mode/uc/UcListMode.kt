package com.wj.commonlib.data.mode.uc

import com.google.gson.JsonObject

/**
 * Uc的新闻列表
 * @author dchain
 * @version 1.0
 * @date 2019/10/23
 */
data class UcListMode(
    val `data`: UcLisData,
    val extension: Extension,
    val message: String,
    val result: Result,
    val status: Int
)

data class UcLisData(
    val articles: JsonObject,
    val banners: List<Any>,
    val items: List<UcItem>
)
data class UcArticleItem(
    val ad_content: AdContent,
    val bottomLeftMark: BottomLeftMark,
    val bottom_left_mark: BottomLeftMarkX,
    val cmt_cnt: Int,
    val dislike_infos: List<DislikeInfo>,
    val enable_dislike: Boolean,
    val grab_time: Long,
    val id: String,
    val item_type: Int,
    val like_cnt: Int,
    val oppose_cnt: Int,
    val origin_src_name: String,
    val publish_time: Long,
    val recoid: String,
    val source_name: String,
    val style_type: Int,
    val summary: String,
    val support_cnt: Int,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val url: String,
    val show_impression_url:String,
    var isRead:Boolean,
    val videos: List<UcVideo>
)

class AdContent(
    val click_ad_url_array:ArrayList<String>,
    val show_ad_url_array:ArrayList<String>
)

data class UcVideo(
    val length: Int,
    val poster: Poster,
    val url: String,
    val video_id: String,
    val view_cnt: Int
)

data class Poster(
    val height: Int,
    val type: String,
    val url: String,
    val width: Int
)

class BottomLeftMark(
    val mark: String,
    val mark_color: Int,
    val mark_icon_url: String
)

class BottomLeftMarkX(
)

data class DislikeInfo(
    val code: Int,
    val msg: String,
    val type: Int
)

data class Thumbnail(
    val height: Int,
    val type: String,
    val url: String,
    val width: Int
)


data class UcItem(
    val id: String,
    val map: String,
    var isRead:Boolean
)