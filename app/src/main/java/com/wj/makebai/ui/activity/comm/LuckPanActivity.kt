package com.wj.makebai.ui.activity.comm

import android.view.LayoutInflater
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.wj.commonlib.data.mode.LuckyMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.weight.NoData
import com.wj.makebai.ui.weight.luckpan.LuckPanAnimEndCallBack
import com.wj.makebai.utils.MbTools
import kotlinx.android.synthetic.main.activity_luckpan.*
import kotlinx.android.synthetic.main.dialog_lucky.view.*

/**
 * 转盘抽奖
 * @author Administrator
 * @version 1.0
 * @date 2019/11/19
 */
class LuckPanActivity : MakeActivity(), View.OnClickListener {
    private var luckList = ArrayList<LuckyMode>()
    private var count = 3
    private var isLoad = false
    private var iad: UnifiedInterstitialAD? = null
    override fun bindLayout(): Int {
        return R.layout.activity_luckpan
    }

    override fun initData() {
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        setThemeColor(R.color.blue2)
        backto_img.setImageResource(R.drawable.ic_back_w)
        title_line.visibility = View.GONE

        setState(NoData.DataState.LOADING, false)
        HttpManager.luckyList {
            luckList = it
            luck_pan.setItems(it.toArrayListStr())
            setState(NoData.DataState.GONE, false)
        }
        luck_count.text = String.format(getString(R.string.luck_count), 3)
        if (Statics.userMode != null) {
            HttpManager.luckyCount {
                count = it
                luck_count.text = String.format(getString(R.string.luck_count), count)
            }
        } else {
            luck_count.text = "登录才能抽奖哦~"
            lucky_recode.visibility = View.GONE
        }

        start.setOnClickListener(this)
        lucky_recode.setOnClickListener(this)
        getIAD()
    }

    /**
     * 加载广告
     */
    private fun getIAD() {
        iad = UnifiedInterstitialAD(
            this,
            StaticData.SPLASH_CP_ID,
            object : UnifiedInterstitialADListener {
                override fun onADExposure() {
                }

                override fun onADOpened() {
                }

                override fun onADClosed() {
                }

                override fun onADLeftApplication() {
                }

                override fun onADReceive() {
                    // onADReceive之后才能调用getAdPatternType()
                    if (iad!!.adPatternType == AdPatternType.NATIVE_VIDEO) {
                        iad!!.setMediaListener(object : UnifiedInterstitialMediaListener {
                            override fun onVideoPageOpen() {
                            }

                            override fun onVideoLoading() {
                            }

                            override fun onVideoReady(p0: Long) {
                            }

                            override fun onVideoInit() {
                            }

                            override fun onVideoPause() {
                            }

                            override fun onVideoPageClose() {
                            }

                            override fun onVideoStart() {
                            }

                            override fun onVideoComplete() {
                            }

                            override fun onVideoError(p0: AdError?) {
                            }

                        })
                    }
                    // onADReceive之后才可调用getECPM()
                    iad!!.show()
                }

                override fun onVideoCached() {
                }

                override fun onNoAD(p0: AdError?) {
                }

                override fun onADClicked() {
                }

            })
        val builder = VideoOption.Builder()
        val option = builder.build()
        iad!!.setVideoOption(option)
        iad!!.setMaxVideoDuration(5)

        /**
         * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前调用setVideoPlayPolicy，有助于提高视频广告的eCPM值 <br/>
         * 如果广告位仅支持图文广告，则无需调用
         */

        /**
         * 设置本次拉取的视频广告，从用户角度看到的视频播放策略<p/>
         *
         * "用户角度"特指用户看到的情况，并非SDK是否自动播放，与自动播放策略AutoPlayPolicy的取值并非一一对应 <br/>
         *
         * 如自动播放策略为AutoPlayPolicy.WIFI，但此时用户网络为4G环境，在用户看来就是手工播放的
         */
        /**
         * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前调用setVideoPlayPolicy，有助于提高视频广告的eCPM值 <br></br>
         * 如果广告位仅支持图文广告，则无需调用
         */
        /**
         * 设置本次拉取的视频广告，从用户角度看到的视频播放策略
         *
         *
         *
         * "用户角度"特指用户看到的情况，并非SDK是否自动播放，与自动播放策略AutoPlayPolicy的取值并非一一对应 <br></br>
         *
         * 如自动播放策略为AutoPlayPolicy.WIFI，但此时用户网络为4G环境，在用户看来就是手工播放的
         */
        iad!!.setVideoPlayPolicy(VideoOption.VideoPlayPolicy.AUTO)
        iad!!.loadAD()
    }

    /**
     * 取出名字
     */
    private fun ArrayList<LuckyMode>.toArrayListStr(): ArrayList<String> {
        val list = ArrayList<String>()
        forEach {
            list.add(it.lucky_name)
        }
        return list
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.start -> {//开始抽奖
                if (isLoad) return
                isLoad = true
                if (MbTools.isLogin(activity)) {
                    HttpManager.luckyResult({
                        for (index in luckList.indices) {
                            if (luckList[index].luckydrawId == it) {
                                luck_pan.setLuckNumber(index)
                                luck_pan.startAnim()
                                luck_pan.luckPanAnimEndCallBack = object : LuckPanAnimEndCallBack {
                                    override fun onAnimEnd(str: String?) {
                                        val dialogView = LayoutInflater.from(activity)
                                            .inflate(R.layout.dialog_lucky, null)
                                        val dialog = ViewControl.customAlertDialog(
                                            activity,
                                            dialogView,
                                            null
                                        )
                                        dialogView.close.setOnClickListener { dialog.cancel() }
                                        dialogView.ok.setOnClickListener { startActivity<ExchangeActivity>() }
                                        dialogView.user_points.text = luckList[index].lucky_name

                                        count -= 1
                                        luck_count.text =
                                            String.format(getString(R.string.luck_count), count)
                                        //刷新用户信息的积分
                                        HttpManager.userDetail({},null)

                                        isLoad = false
                                    }
                                }
                                break
                            }
                        }
                    }, fun(_, _, _) {
                        isLoad = false
                    })
                } else {
                    showTip("今天次数用完了哦~")
                }
            }
            R.id.lucky_recode -> {//中奖记录
                startActivity<LuckyRecodeActivity>()
            }
        }
    }
}