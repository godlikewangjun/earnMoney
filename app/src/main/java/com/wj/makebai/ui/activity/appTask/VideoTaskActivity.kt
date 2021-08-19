package com.wj.makebai.ui.activity.appTask

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import com.qq.e.ads.rewardvideo.RewardVideoAD
import com.qq.e.ads.rewardvideo.RewardVideoADListener
import com.qq.e.comm.util.AdError
import com.wj.commonlib.http.HttpManager
import com.wj.ktutils.WjSP
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.statice.XmlCodes

/**
 * 激励视频广告
 * @author Administrator
 * @version 1.0
 * @date 2019/12/21
 */
class VideoTaskActivity : Activity() {
    //广告
    private var rewardVideoAD: RewardVideoAD? = null
    private var adLoaded  = false//广告加载成功标志
    private var videoCached  = false//视频素材文件下载完成标志


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAd()
    }
    /**
     * 加载激励视频
     */
    private fun loadAd() {
        // 1. 初始化激励视频广告
        rewardVideoAD = RewardVideoAD(
            this,
            StaticData.JDT_JLVIDEO_AD,
            object :
                RewardVideoADListener {
                override fun onADExpose() {
                }

                override fun onADClick() {
                }

                override fun onVideoCached() {
                    videoCached = true
                }

                override fun onReward(p0: MutableMap<String, Any>?) {//奖励到账
                     HttpManager.videoTask({
                        showTip(String.format(getString(R.string.add_points, it.points)))
                         WjSP.getInstance()
                            .setValues(XmlCodes.VIDEOWATCH, System.currentTimeMillis())
                    }) {
                        finish()
                    }
                }

                override fun onADClose() {
                    finish()
                }

                override fun onADLoad() {
                    adLoaded = true
                    // 3. 展示激励视频广告
                    if (adLoaded && rewardVideoAD != null) { //广告展示检查1：广告成功加载，此处也可以使用videoCached来实现视频预加载完成后再展示激励视频广告的逻辑
                        if (!rewardVideoAD!!.hasShown()) { //广告展示检查2：当前广告数据还没有展示过
                            val delta: Long = 1000 //建议给广告过期时间加个buffer，单位ms，这里demo采用1000ms的buffer
                            //广告展示检查3：展示广告前判断广告数据未过期
                            if (SystemClock.elapsedRealtime() < rewardVideoAD!!.expireTimestamp - delta) {
                                rewardVideoAD!!.showAD()
                            }
                        }
                    }
                }

                override fun onVideoComplete() {
                    finish()
                }

                override fun onError(p0: AdError?) {
                }

                override fun onADShow() {
                }

            })
        adLoaded = false
        videoCached = false
        // 2. 加载激励视频广告
        // 2. 加载激励视频广告
        rewardVideoAD!!.loadAD()
    }
}