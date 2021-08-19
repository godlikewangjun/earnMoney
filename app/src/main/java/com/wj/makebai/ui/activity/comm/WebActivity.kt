package com.wj.makebai.ui.activity.comm

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.abase.util.AbLogUtil
import com.abase.util.AbViewUtil
import com.abase.view.weight.LoadWeb
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.nativ.NativeExpressMediaListener
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.export.external.interfaces.WebResourceError
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.umeng.analytics.MobclickAgent
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.utils.ShareManager
import com.wj.eventbus.WjEventBus
import com.wj.im.utils.GDTAdTools
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.statice.UmEventCode
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.control.CommControl
import com.wj.makebai.utils.MbTools.urlCanLoad
import com.xiaomi.push.js
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.android.synthetic.main.articlecontent_item_layout.*
import kotlinx.android.synthetic.main.web_layout.*
import java.net.URISyntaxException

/**
 * 网页浏览器
 * @author Admin
 * @version 1.0
 * @date 2018/6/20
 */
class WebActivity : MakeActivity() {
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    private var mADManager: NativeExpressAD? = null//腾讯广告
    override fun bindLayout(): Int {
        return R.layout.web_layout
    }

    override fun initData() {
        val music = HashMap<String, Any>()

        webView.webViewClient = client
//        webView.settings.displayZoomControls=false
//        webView.settings.setSupportZoom(false)
//        webView.settings.builtInZoomControls = false


        if (intent.hasExtra("title")) {
            title_content.text = intent.getStringExtra("title")
            music["title"] = intent.getStringExtra("title")//自定义参数
        }
        if (intent.hasExtra("content")) {
            webView.loadHtml(intent.getStringExtra("content"))
        }
        if (intent.hasExtra("url")) {
            webView.setUrl(intent.getStringExtra("url"))
            music["url"] = intent.getStringExtra("url")//自定义参数

            MobclickAgent.onEventObject(activity, UmEventCode.UM_WEBVIEW, music)
        }
        if (intent.hasExtra("data")) {
            webView.postUrl(
                intent.getStringExtra("url"),
                intent.getStringExtra("data").toByteArray()
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        //关闭
        val close = ImageView(activity)
        val layoutParams =
            RelativeLayout.LayoutParams(
                AbViewUtil.dp2px(activity, 18f),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.lin_back)
        close.layoutParams = layoutParams
        close.setImageResource(R.drawable.ic_close)
        title.addView(close)

        close.setOnClickListener {
            finish()
        }
        lin_back.setOnClickListener { onBackPressed() }

        if (intent.hasExtra("url")) {
            //分享
            other_icon.setImageResource(R.drawable.ic_artmore)
            other_down.setOnClickListener {
                initMenuFragment()

                showContextMenuDialogFragment()
            }
        }

        //浏览器处理
        CommControl.noNetWork(activity, nodata, title_content, webView, progress_horizontal, null)
        if (intent.hasExtra("ad"))
            loadAD()
        webView.setDownloadListener { s, s2, s3, s4, l ->
            AlertDialog.Builder(activity).setTitle("下载").setMessage("是否开始下载$s2").setNegativeButton(
                "确定"
            ) { dialog, _ ->
                webView.onDownloadStart(s, s2, s3, s4, l)
                dialog?.dismiss()
            }.setPositiveButton("取消", null).show()
        }
    }

    private var client: WebViewClient = object : WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            if (url.isNull()) {
                return false
            }
            try {
                if (!url.urlCanLoad()) {
                    val dialog = AlertDialog.Builder(activity).create()
                    dialog.setTitle("是否跳转")
                    dialog.setMessage("即将跳转到其他应用")
                    dialog.setButton(
                        DialogInterface.BUTTON_POSITIVE, "确定"
                    ) { _, _ ->
                        AbLogUtil.d(WebActivity::class.java, url)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            view.context.startActivity(intent)
                        } catch (e: Exception) {
                            showTip("无法打开")
                        }
                        dialog.cancel()
                    }
                    dialog.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        "取消"
                    ) { _, _ -> dialog.cancel() }
                    dialog.show()
                    return true
                }
            } catch (e: Exception) {
                return false
            }
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(
            webView: WebView,
            s: String
        ) {
            super.onPageFinished(webView, s)
            WjEventBus.getInit().post(LoadWeb.LOADFINSH, "")

            val js = "window.localStorage.setItem('access_token','${Statics.DZ_LOGIN_INFO?.data?.attributes?.access_token}');window.localStorage.setItem('user_id',${Statics.DZ_LOGIN_INFO?.data?.id});"
            val jsUrl =
                "javascript:(function({var localStorage = window.localStorage;localStorage.setItem('access_token','${Statics.DZ_LOGIN_INFO?.data?.attributes?.access_token}')})()"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(js, null);
            } else {
                webView.loadUrl(jsUrl);
                webView.reload();
            }
        }

        override fun onReceivedError(
            webView: WebView,
            i: Int,
            s: String,
            s1: String
        ) {
            super.onReceivedError(webView, i, s, s1)
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "")
        }

        override fun onReceivedError(
            webView: WebView,
            webResourceRequest: WebResourceRequest,
            webResourceError: WebResourceError
        ) {
            super.onReceivedError(webView, webResourceRequest, webResourceError)
            WjEventBus.getInit().post(LoadWeb.LOADERROE, "")
        }

        override fun onReceivedSslError(
            webView: WebView,
            sslErrorHandler: SslErrorHandler,
            sslError: SslError
        ) {
            sslErrorHandler.proceed()
        }
    }

    /**
     * 拉取广告
     */
    private fun loadAD() {

        val adSize = ADSize(
            ADSize.FULL_WIDTH,
            ADSize.AUTO_HEIGHT
        ) // 消息流中用AUTO_HEIGHT

        mADManager = NativeExpressAD(
            activity,
            adSize,
            StaticData.JDT_ART_DETAIL_AD,
            object : NativeExpressAD.NativeExpressADListener {
                override fun onADCloseOverlay(p0: NativeExpressADView?) {
                }

                override fun onADLoaded(adList: MutableList<NativeExpressADView>?) {
                    val view = adList!![0]
                    if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                        view.setMediaListener(mediaListener)
                    }
                    express_ad_container.addView(view)
                    view.render() // 调用render方法后sdk才会开始展示广告

                    express_ad_container.postDelayed({
                        express_ad_container.visibility = View.GONE
                    }, 10 * 1000)
                }

                override fun onADOpenOverlay(p0: NativeExpressADView?) {
                }

                override fun onRenderFail(p0: NativeExpressADView?) {
                }

                override fun onADExposure(p0: NativeExpressADView?) {
                }

                override fun onADClosed(adView: NativeExpressADView?) {
                }

                override fun onADLeftApplication(p0: NativeExpressADView?) {
                }

                override fun onNoAD(p0: AdError?) {
                }

                override fun onADClicked(p0: NativeExpressADView?) {
                }

                override fun onRenderSuccess(p0: NativeExpressADView?) {
                }

            }
        )
        mADManager!!.setMaxVideoDuration(10)
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
        mADManager!!.setVideoPlayPolicy(
            GDTAdTools.getVideoPlayPolicy(
                VideoOption.AutoPlayPolicy.ALWAYS,
                activity!!
            )
        ) // 本次拉回的视频广告，在用户看来是否为自动播放的

        mADManager!!.loadAD(1)
    }

    private val mediaListener: NativeExpressMediaListener = object :
        NativeExpressMediaListener {
        override fun onVideoInit(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoLoading(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoCached(p0: NativeExpressADView?) {
        }

        override fun onVideoReady(
            nativeExpressADView: NativeExpressADView,
            l: Long
        ) {
        }

        override fun onVideoStart(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoPause(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoComplete(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoError(
            nativeExpressADView: NativeExpressADView,
            adError: AdError
        ) {
        }

        override fun onVideoPageOpen(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoPageClose(nativeExpressADView: NativeExpressADView) {
        }
    }

    /**
     * 初始化分享
     */
    private fun initMenuFragment() {
        val menuParams = MenuParams(
            actionBarSize = resources.getDimension(R.dimen.tab_height).toInt(),
            menuObjects = getMenuObjects(),
            isClosableOutside = false
        ).apply {
            isClosableOutside = true
            isFitsSystemWindow = true
        }
        contextMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams).apply {
            menuItemClickListener = { _, position ->
                val listener = object : UMShareListener {
                    override fun onResult(p0: SHARE_MEDIA?) {
                    }

                    override fun onCancel(p0: SHARE_MEDIA?) {
                    }

                    override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {
                    }

                    override fun onStart(p0: SHARE_MEDIA?) {
                    }

                }
                val link =
                    if (intent.hasExtra("uc")) intent.getStringExtra("uc") else intent.getStringExtra(
                        "url"
                    )
                var type: SHARE_MEDIA? = null
                when (position) {
                    1 -> {//微信好友
                        type = SHARE_MEDIA.WEIXIN
                    }
                    2 -> {//微信朋友圈
                        type = SHARE_MEDIA.WEIXIN_CIRCLE
                    }
                    3 -> {//qq
                        type = SHARE_MEDIA.QQ
                    }
                    4 -> {//qq空间
                        type = SHARE_MEDIA.QZONE
                    }
                }
                if (type != null) {
                    ShareManager.shareManager!!.share(
                        activity!!,
                        type,
                        link,
                        if (!title_content.text.isNull()) title_content.text.toString() else StaticData.appShareInfo!!.appname,
                        null,
                        StaticData.appShareInfo!!.describe,
                        listener
                    )
                }
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        val close = MenuObject().apply { setResourceValue(R.drawable.ic_close) }
        val send = MenuObject(getString(R.string.wx)).apply { setResourceValue(R.drawable.ic_wx) }
        val addFriend =
            MenuObject(getString(R.string.wx_circle)).apply { setResourceValue(R.drawable.ic_pyq) }
        val qq = MenuObject(getString(R.string.qq)).apply { setResourceValue(R.drawable.ic_qq) }
        val qqzone =
            MenuObject(getString(R.string.qq_zone)).apply { setResourceValue(R.drawable.ic_qqzone) }

        add(close)
        add(send)
        add(addFriend)
        add(qq)
        add(qqzone)
    }

    private fun showContextMenuDialogFragment() {
        if (supportFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null)
            contextMenuDialogFragment.show(supportFragmentManager, ContextMenuDialogFragment.TAG)
    }


    override fun onBackPressed() {
        if (webView!!.canGoBack())
            webView!!.goBack()
        else
            finish()

    }

    override fun onDestroy() {
        express_ad_container.handler.removeCallbacksAndMessages(null)
        WjEventBus.getInit().remove(LoadWeb.LOADFINSH)
        webView.destroy()
        super.onDestroy()
    }
}