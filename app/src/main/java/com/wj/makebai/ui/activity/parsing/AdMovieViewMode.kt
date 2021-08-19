package com.wj.makebai.ui.activity.parsing

import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.*
import com.qq.e.ads.nativ.widget.NativeAdContainer
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.wj.makebai.statice.StaticData
import com.wj.makebai.utils.MbTools.load
import kotlinx.android.synthetic.main.advideoview_layout.view.*
import kotlin.collections.ArrayList


/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/5
 */
class AdMovieViewMode : NativeADUnifiedListener {


    private var mDownloadButton: TextView? = null
    private var mAdData: NativeUnifiedADData? = null
    private val mHandler = H()
    private val MSG_INIT_AD = 0
    private val MSG_VIDEO_START = 1
    private val AD_COUNT = 1

    // 与广告有关的变量，用来显示广告素材的UI
    private var mAdManager: NativeUnifiedAD? = null
    private var mMediaView: MediaView? = null
    private var mImagePoster: ImageView? = null
    private var mContainer: NativeAdContainer? = null
    private var native3imgContainer: LinearLayout? = null

    private var mIsLoading = false

    private var mBindToCustomView = false

    private var context:Context?=null
    private var contentView: View?=null
    var onVideoCompleted:(()->Unit)?=null

    fun init(context: Context,contentView: View,lifecycle: Lifecycle){
        this.context=context
        this.contentView=contentView
        mMediaView = contentView.gdt_media_view
        mImagePoster = contentView.img_poster
        mDownloadButton = contentView.btn_download
        mContainer = contentView.native_ad_container
        native3imgContainer = contentView.native_3img_ad_container

        mAdManager = NativeUnifiedAD(context, StaticData.JDT_MOVIEW_AD, this)
        mAdManager!!.setMinVideoDuration(5)
        mAdManager!!.setMaxVideoDuration(10)

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (Lifecycle.Event.ON_DESTROY == event ) {
                    onDestroy()
                }else  if (Lifecycle.Event.ON_RESUME == event ) {
                    onResume()
                }
            }
        })
        loadAd()
    }
    fun loadAd(){
        if (mIsLoading) {
            return
        }
        if (mAdData != null) {
            mAdData!!.destroy()
        }
        mIsLoading = true
        mContainer!!.visibility = View.VISIBLE
        mAdManager!!.loadData(AD_COUNT)
    }
    fun closeAd(){
        mContainer!!.visibility = View.GONE
        if (mAdData != null) {
            mAdData!!.destroy()
        }
    }



    override fun onADLoaded(ads: List<NativeUnifiedADData?>?) {
        mIsLoading = false
        if (ads != null && ads.isNotEmpty()) {
            val msg: Message = Message.obtain()
            msg.what = MSG_INIT_AD
            mAdData = ads[0]
            msg.obj = mAdData
            mHandler.sendMessage(msg)
        }
    }

    private fun initAd(ad: NativeUnifiedADData) {
        renderAdUi(ad)
        //点击直接下载（App广告）或进入落地页
        val clickableViews: MutableList<View?> = ArrayList()
        val customClickableViews: MutableList<View?> = ArrayList()
        if (mBindToCustomView) {
            customClickableViews.add(mDownloadButton)
        } else {
            clickableViews.add(mDownloadButton)
        }
        if (ad.adPatternType == AdPatternType.NATIVE_2IMAGE_2TEXT ||
            ad.adPatternType == AdPatternType.NATIVE_1IMAGE_2TEXT
        ) {
            // 双图双文、单图双文：注册mImagePoster的点击事件
            clickableViews.add(mImagePoster)
        } else if (ad.adPatternType != AdPatternType.NATIVE_VIDEO) {
            // 三小图广告：注册native_3img_ad_container的点击事件
            clickableViews.add(native3imgContainer)
        }
        //作为customClickableViews传入，点击不进入详情页，直接下载或进入落地页，图文、视频广告均生效，
        ad.bindAdToView(context, mContainer, null, clickableViews, customClickableViews)
        ad.setNativeAdEventListener(object : NativeADEventListener {
            override fun onADExposed() {
            }

            override fun onADClicked() {
            }

            override fun onADError(error: AdError) {
            }

            override fun onADStatusChanged() {
                updateAdAction(mDownloadButton, ad)
            }
        })
        if (ad.adPatternType == AdPatternType.NATIVE_VIDEO) {
            mHandler.sendEmptyMessage(MSG_VIDEO_START)
            val videoOption = VideoOption.Builder().setAutoPlayMuted(true).build()
            ad.bindMediaView(mMediaView, videoOption, object : NativeADMediaListener {
                override fun onVideoInit() {
                }

                override fun onVideoLoading() {
                }

                override fun onVideoReady() {
                }

                override fun onVideoLoaded(videoDuration: Int) {
                }

                override fun onVideoStart() {
                    contentView!!.btn_download.isVisible=true
                }

                override fun onVideoPause() {
                }

                override fun onVideoResume() {
                }

                override fun onVideoCompleted() {
                    mContainer!!.isVisible = false
                    contentView!!.btn_download.isVisible=true
                    onVideoCompleted?.invoke()
                }

                override fun onVideoError(error: AdError) {
                    mContainer!!.isVisible = false
                    onVideoCompleted?.invoke()
                    contentView!!.btn_download.isVisible=false
                }

                override fun onVideoStop() {
                }

                override fun onVideoClicked() {
                }
            })
        }
        updateAdAction(mDownloadButton, ad)
    }

    private fun onResume() {
        if (mAdData != null) {
            // 必须要在Actiivty.onResume()时通知到广告数据，以便重置广告恢复状态
            mAdData!!.resume()
        }
    }

    private fun renderAdUi(ad: NativeUnifiedADData) {
        val patternType = ad.adPatternType
        if (patternType == AdPatternType.NATIVE_2IMAGE_2TEXT
            || patternType == AdPatternType.NATIVE_VIDEO
        ) {
            setVisibilityFor3Img(false)
            mMediaView?.visibility = if (patternType == AdPatternType.NATIVE_VIDEO) View.VISIBLE else View.GONE
            mImagePoster?.visibility = View.VISIBLE
            contentView!!.img_logo.load(ad.iconUrl)
            contentView!!.img_poster.load(ad.imgUrl)
            contentView!!.text_title.text=ad.title
            contentView!!.text_desc.text=ad.desc
        } else if (patternType == AdPatternType.NATIVE_3IMAGE) {
            contentView!!.img_1.load(ad.imgList[0])
            contentView!!.img_2.load(ad.imgList[1])
            contentView!!.img_3.load(ad.imgList[2])
            contentView!!.text_title.text=ad.title
            contentView!!.text_desc.text=ad.desc
            setVisibilityFor3Img(true)
            mImagePoster!!.visibility = View.GONE
            mMediaView!!.visibility = View.GONE
        } else if (patternType == AdPatternType.NATIVE_1IMAGE_2TEXT) {
            contentView!!.img_logo.load(ad.imgUrl)
            contentView!!.img_poster.setImageDrawable(null)
            contentView!!.text_title.text=ad.title
            contentView!!.text_desc.text=ad.desc
            mImagePoster!!.visibility = View.GONE
            setVisibilityFor3Img(false)
            mMediaView!!.visibility = View.GONE
        }
    }

    private fun setVisibilityFor3Img(is3img: Boolean) {
        contentView!!.img_logo.visibility = if (is3img) View.INVISIBLE else View.VISIBLE
        contentView!!.native_3img_ad_container.visibility = if (is3img) View.VISIBLE else View.GONE
    }

    private fun onDestroy() {
        if (mAdData != null) {
            // 必须要在Actiivty.destroy()时通知到广告数据，以便释放内存
            mAdData!!.destroy()
        }
    }

    fun updateAdAction(button: TextView?, ad: NativeUnifiedADData) {
        if (!ad.isAppAd) {
            button!!.setText("浏览")
            return
        }
        when (ad.appStatus) {
            0 -> button!!.text = "下载"
            1 -> button!!.text = "启动"
            2 -> button!!.text = "更新"
            4 -> button!!.text = ad.progress.toString() + "%"
            8 -> button!!.text = "安装"
            16 -> button!!.text = "下载失败，重新下载"
            else -> button!!.text = "浏览"
        }
    }

    override fun onNoAD(error: AdError) {
        mIsLoading = false
        mContainer!!.isVisible = false
        onVideoCompleted?.invoke()
    }

    private inner  class H : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_INIT_AD -> {
                    val ad = msg.obj as NativeUnifiedADData
                    initAd(ad)
                }
                MSG_VIDEO_START -> {
                    mImagePoster!!.visibility = View.GONE
                    mMediaView!!.visibility = View.VISIBLE
                }
            }
        }
    }


}