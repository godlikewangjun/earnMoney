package com.wj.makebai.statice

import com.abase.util.GsonUtil
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.Announcement
import com.wj.commonlib.data.mode.AppInfo
import com.wj.commonlib.data.mode.InitMode
import com.wj.ktutils.WjSP
import com.wj.makebai.R

/**
 * 存入重要的静态参数值
 * @author dchain
 * @version 1.0
 * @date 2019/10/8
 */
object StaticData {
    /**吐槽链接*/
    val TUCAO = "https://support.qq.com/products/91289?d-wx-push=1"
    /**微信APPID*/
    val WX_APPID = "wx446f63f581e24c44"
    /**微信KEY*/
    val WX_KEY = "e536e999c7fed28c056240fadb89766b"
    /**QQ APPID*/
    val QQ_APPID = "101811340"
    /**QQ KEy*/
    val QQ_KEy = "4cae9961a3499713db6cc98b342fd0fd"
    /**广点通appid*/
    val GDT_APPID = "1109891723"
    /**友盟appid*/
    val UM_APPID = "5d6c81300cafb2c0b300085c"
    /**友盟secret*/
    val UM_PUSH_SECRET = "4a3142947b49ec5db4b58c832647f04d"
    /**有米id*/
    val YM_APPID = "b9c3249b19cc9b3d"
    /**有米SECRET*/
    val YM_SECRET = "997c951bdea979a7"
    /**闲玩APPID*/
    val XW_APPID = "4405"
    /**闲玩SECRET*/
    val XW_SECRET = "yy2fuybn297v539c"
    /**腾讯云IM APPKey*/
    val TX_APPID = 1400378178
    /**腾讯云IM SECRET*/
    val TX_SECRET = "54984741e8f74779fac208b49f8bfca2604cc2736f57fea1db093bdea2960f66"
    /**腾讯云IM APPKey*/
    val BUGLY = "769d9e3a93"



    /**广点通开屏的广告位*/
    val SPLASH_POS_ID = "4070696126242589"
    /**广点通插屏广告位*/
    val SPLASH_CP_ID = "3000899187740949"
    /**广点通BANNER广告位*/
    val SPLASH_BANNER_ID = "9020797128951190"
    /**广点通文章信息流*/
    val JDT_ART_AD = "4030094448122363"
    /**广点通文章详情*/
    val JDT_ART_DETAIL_AD = "8030999408828457"
    /**广点通首页的原生banner*/
    val JDT_HOME_BANNER_AD = "5050099488921508"
    /**视频流的广告*/
    val JDT_VIDEO_AD = "3090298561674051"
    /**激励适配的广告*/
    val JDT_JLVIDEO_AD = "3070893581273034"
    /**视频开头的广告*/
    val JDT_MOVIEW_AD = "3041660101679325"



    /**论坛链接*/
    var BBS = ""

    /**APP分享的信息存储*/
    var appShareInfo: AppInfo? = null
        set(value) {
            field = value
            WjSP.getInstance()
                .setValues(XmlCodes.APP_SHARE, GsonUtil.gson2String(field))
        }
        get() {
            val appShareInfo =
                WjSP.getInstance()
                    .getValues<String>(XmlCodes.APP_SHARE, null)
            return if (field == null && appShareInfo != null) GsonUtil.gson2Object(
                appShareInfo,
                AppInfo::class.java
            ) else if (field == null) AppInfo(
                "",
                BaseApplication.mApplication!!.get()!!.getString(R.string.app_name),
                "视频、漫画、小说应有尽有！", "闲着也是闲着，不如攒点钱！",""
            ) else field
        }
    /**公告*/
    var announcement: Announcement? = null
    /**初始化的信息存储*/
    var initMode: InitMode? = null
        set(value) {
            field = value
            WjSP.getInstance()
                .setValues(XmlCodes.INIT_DATA, GsonUtil.gson2String(field))
        }
        get() {
            return if (field != null) field else
                GsonUtil.gson2Object(
                    WjSP.getInstance()
                        .getValues<String>(XmlCodes.INIT_DATA, null), InitMode::class.java
                )
        }

}