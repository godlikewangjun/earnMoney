package com.wj.commonlib.data.mode

import android.text.Html
import com.sunfusheng.marqueeview.IMarqueeItem


/**
 * 初始化mode
 * @author dchain
 * @version 1.0
 * @date 2019/10/8
 */
data class InitMode(
    val ad: AdMode,
    val articleTypes: List<ArticleKeyMode>,
    val isInto: IsInto,
    val bbs: String,
    /**文章搜索关键词*/
    var article_kw:String = "",
    /**视频搜索关键词*/
    var video_kw:String = "",
    /**小说搜索关键词*/
    var novel_kw:String = "",
    /**漫画搜索关键词*/
    var comic_kw :String= "",
    val app_guide:String,
    val earn_guide:String,
    val scale :Double,
    val news :Boolean,
    val service_token:String,
    val appShareInfo: AppInfo,
    val homeBanner:ArrayList<AdMode>,
    val homeType:ArrayList<HomeToolsMode>,
    val announcement: Announcement,
    val message:List<Message>
)

data class Message (
    val content: String,
    val title: String,
    val messageid:Int,
    val creatime: String,
    val type:String,
    val toDate:String,
    val level:Int
): IMarqueeItem {
    override fun marqueeMessage(): CharSequence {
        return Html.fromHtml(title)
    }
}

data class IsInto(
    val configid: Int,
    val creatime: String,
    val toDate: String,
    val type: Int,
    val userid: Int,
    val value: String
)

data class AppInfo(
    val appicon: String,
    val appname: String,
    val describe: String,
    val slogan: String,
    val downLoad: String
)
data class Announcement(
    val toDate: String,
    val value: String,
    val type: Int
)
