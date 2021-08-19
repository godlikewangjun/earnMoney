package com.wj.im.utils

import android.content.Context
import android.net.ConnectivityManager
import com.qq.e.ads.cfg.VideoOption

/**
 * 广点通广告
 * @author Administrator
 * @version 1.0
 * @date 2019/12/19
 */
object GDTAdTools {
    var FIRST_AD_POSITION = 1 // 第一条广告的位置
    var ITEMS_PER_AD = 5 // 每间隔10个条目插入一条广告
    var AD_COUNT = 3 //加载的广告条数

    /**
     * 获取如果可以播放视频的环境
     */
    fun getVideoPlayPolicy(autoPlayPolicy: Int, context: Context): Int {
        return when (autoPlayPolicy) {
            VideoOption.AutoPlayPolicy.ALWAYS -> {
                VideoOption.VideoPlayPolicy.AUTO
            }
            VideoOption.AutoPlayPolicy.WIFI -> {
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected) VideoOption.VideoPlayPolicy.AUTO else VideoOption.VideoPlayPolicy.MANUAL
            }
            VideoOption.AutoPlayPolicy.NEVER -> {
                VideoOption.VideoPlayPolicy.MANUAL
            }
            else -> VideoOption.VideoPlayPolicy.UNKNOWN
        }
    }
}