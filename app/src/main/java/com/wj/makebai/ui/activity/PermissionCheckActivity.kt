package com.wj.makebai.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.abase.okhttp.OhHttpClient
import com.bumptech.glide.Glide
import com.qq.e.ads.splash.SplashAD
import com.qq.e.ads.splash.SplashADListener
import com.qq.e.comm.util.AdError
import com.umeng.analytics.MobclickAgent
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.data.mode.ArticleKeyMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.TaskHallRequests
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.statices.XmlConfigs
import com.wj.commonlib.utils.MiitHelper
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.PdtDb
import com.wj.makebai.statice.StaticData
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.comic.ComicListActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.activity.novel.NovelListActivity
import com.wj.makebai.ui.activity.parsing.VipActivity
import com.wj.makebai.ui.activity.shortscut.ScanActivity
import com.wj.makebai.ui.fragment.TaskHallFragment
import com.wj.makebai.ui.weight.PermissionDialog
import com.wj.makebai.utils.MbTools
import com.wj.permission.PermissionUtils
import ebz.lsds.qamj.os.df.DiyOfferWallManager
import kotlinx.android.synthetic.main.activity_splash.*
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import org.apache.http.conn.ssl.SSLSocketFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * 请求权限
 */
class PermissionCheckActivity : AppCompatActivity(), SplashADListener {

    private var isPause = false
    private var canJump = false
    private var isTiming = false
    private var scheme = ""
    private var schemeType = ""//跳转类型
    private var splashAD: SplashAD? = null
    private var isShowAd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreen()

        setContentView(R.layout.activity_splash)

//        if (MainActivity.isLive) {//intent.hasExtra("Notification") &&
//            finish()
//            return
//        }
        //判断是白天还是晚上
//        if (getCurrentTime()) {
//            img_gg_c.setAnimation("app_loading_night.json")
//        } else {
//            img_gg_c.setAnimation("app_loading.json")
//        }

        BaseApplication.mHandler.postDelayed({
            if (Build.VERSION.SDK_INT >= 22) {
                if (WjSP.getInstance().getValues(XmlCodes.APP_FIRST, "").isNull())
                    PermissionDialog(this).setOnClickListener {
                        permissionCheck()
                    }.show()
                else permissionCheck()

            } else {
                initData()
            }
            WjSP.getInstance().setValues(XmlCodes.APP_FIRST, "false")
        }, 300)
    }

    /**
     * 权限请求
     */
    private fun permissionCheck() {
        PermissionUtils.permission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        ).rationale(object : PermissionUtils.OnRationaleListener {
            override fun rationale(shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) {
                shouldRequest.again(true)
            }
        }).callback(object : PermissionUtils.FullCallback {
            override fun onGranted(permissionsGranted: java.util.ArrayList<String>) {
                initData()
            }

            override fun onDenied(
                permissionsDeniedForever: java.util.ArrayList<String>?,
                permissionsDenied: java.util.ArrayList<String>
            ) {
                showTip("部分功能将不可用")
                initData()
            }
        }).request()
    }

    /**
     * 适配刘海屏
     */
    private fun fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            // 设置页面全屏显示
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            // 设置页面延伸到刘海区显示
            window.attributes = lp
        }
    }

    /**
     * 跳转
     */
    fun openMainActivity() {
        if (canJump) {
            img_gg_c.pauseAnimation()
            //日夜间模式切换
            val dn = WjSP.getInstance().getValues<Boolean>(XmlCodes.APP_DN, false)
            if (dn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)//夜间模式
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)//白天模式
            }
            if (MainActivity.isLive) {
                MbTools.schemeOpen(this, scheme, schemeType)
            } else {
                if (!scheme.isNull()) {
                    startActivity<MainActivity>("scheme" to scheme, "schemeType" to schemeType)
                } else startActivity<MainActivity>()
            }
            finish()
        } else canJump = true

    }

    /**
     * 初始化数据
     */
    private fun initData() {
        //防止6.0.1的连不上服务器
        SSLSocketFactory.getSocketFactory().hostnameVerifier = AllowAllHostnameVerifier()
        //第一次安装需要显示隐私协议还有版权声明
        if (!WjSP.getInstance().getValues<Boolean>(
                XmlConfigs.ISAGRENN,
                false
            )
        ) {
            HttpManager.appRuleTips {
                if (!isDestroyed) MbTools.showRules(this, it.value) { isOk ->
                    if (isOk) {
                        WjSP.getInstance().setValues(
                            XmlConfigs.ISAGRENN,
                            true
                        )
                        initData()
                    } else finish()
                }
            }
            return
        }
        if (intent.scheme != null) {
            if (intent.data!!.getQueryParameter("id") != null) {//跳转到文章的
                scheme = intent.data!!.getQueryParameter("id")!!
            } else if (intent.data!!.getQueryParameter("type") != null) {
                schemeType = intent.data!!.getQueryParameter("type")!!
            }

        }
        //更新
//        HttpManager.update({
//            if (!isDestroyed) ViewControl.update(this, it, false)
//        }, fun(_, _, _) {
//        }) {
//
//        }
        getInitData()
    }

    /**
     * 获取初始化的数据
     */
    private fun getInitData(){
        HttpManager.init({
            StaticData.initMode = it
            if (!isDestroyed) {
                //频道
                PdtDb.select(0) { arts ->
                    if (it.articleTypes == null || it.articleTypes.isEmpty()) return@select
                    val oldVersion = WjSP.getInstance()
                        .getValues(XmlCodes.PD_VERSION, 0)
                    val defaults = ArrayList<ArticleKeyMode>()
                    val others = ArrayList<ArticleKeyMode>()
                    var version = 0
                    for (index in it.articleTypes) {
                        if (index.article_key == "version") {
                            version = index.describe.toInt()
                            continue
                        }
                        if (index.type == 0) defaults.add(index) else others.add(index)
                    }
                    if (arts.isEmpty() || oldVersion < version) {

                        PdtDb.insert(defaults, 0)
                        PdtDb.insert(others, 1)

                        WjSP.getInstance()
                            .setValues(XmlCodes.PD_VERSION, version)
                    }
                }

                //分享出去的信息
                if (it.appShareInfo != null) {
                    StaticData.appShareInfo = it.appShareInfo
                }
                //公告
                if (it.announcement != null) {
                    StaticData.announcement = it.announcement
                }
                //论坛地址
                if (it.bbs != null) {
                    StaticData.BBS = it.bbs
                }
                //获取uc token
                if (Statics.UC_TOKEN.isNull()) Statics.UC_TOKEN = it.service_token

                //维护公告通知，不能进
                if (it.isInto != null) {
                    AlertDialog.Builder(this).setTitle("提示").setMessage(it.isInto.value)
                        .setPositiveButton(
                            "确认"
                        ) { _, _ ->
                            //开屏
                            if (it.ad.state == 0) {
                                Glide.with(this@PermissionCheckActivity).load(it.ad.img_url)
                                    .into(img_gg)
                                delayTime()
                            } else {
                                delayTime()
                            }
                        }.show()
                } else {
                    isShowAd = true
                    //开屏
                    if (it.ad.state == 0) {
                        Glide.with(this).load(it.ad.img_url).into(img_gg)
                        delayTime()
                    } else {
//                        val time = WjSP.getInstance().getValues(XmlCodes.SPLASHTIME, 0L)
//                        if (time != 0L && System.currentTimeMillis() - time > 1000 * 60 || time == 0L)
                        //加载广点通
                        fetchSplashAD(
                            this,
                            splash_container,
                            fr_skip,
                            getPosId(),
                            this,
                            0
                        )
                    }

                    delayTime()

                    //有米
                    DiyOfferWallManager.getInstance(this).onAppLaunch()

                    //9.0才执行oaid
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        //获取oaid
                        MiitHelper(object : MiitHelper.AppIdsUpdater {
                            override fun OnIdsAvalid(ids: String?) {
                                Statics.OAID = ids!!
                            }

                        }).getDeviceIds(this)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                        initShortsCut()

                    TaskHallRequests.getTaskType {
                        TaskHallFragment.types = it
                    }
                    HttpManager.allList<AppTaskMode>(Urls.taskHall, 1, 0, "", null, {
                        TaskHallFragment.list = it
                    }, fun(_, _, _) {})
                }
            }
        }, fun(code, _, _) {
            val dialog = AlertDialog.Builder(this).setTitle("错误提示")
                .setPositiveButton(
                    "确定"
                ) { dialog, _ ->
                    dialog?.cancel()
                    this@PermissionCheckActivity.finish()
                }
            when (code) {
                503 -> dialog.setMessage("连接超时，请退出重新打开")
                -1 -> dialog.setMessage("网络连接错误,请检查网络设置")
                else -> dialog.setMessage("无法连接服务器，请检查网络设置")
            }
            dialog.setOnCancelListener {
                finish()
            }
            if (!isDestroyed) dialog.show()
        })
    }

    /**
     * 初始化shortcut
     */

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun initShortsCut() {
        val shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)

        val shortcuts = ArrayList<ShortcutInfo>()
        var intent = Intent(this, ScanActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        var shortcut = ShortcutInfo.Builder(this, "id1")
            .setShortLabel("扫一扫")
            .setLongLabel("扫一扫")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_ss))
            .setIntent(intent)
            .build()

        shortcuts.add(shortcut)

        intent = Intent(this, NovelListActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        shortcut = ShortcutInfo.Builder(this, "id2")
            .setShortLabel("看小说")
            .setLongLabel("看小说")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_novel))
            .setIntent(intent)
            .build()

        shortcuts.add(shortcut)

        intent = Intent(this, ComicListActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        shortcut = ShortcutInfo.Builder(this, "id3")
            .setShortLabel("看漫画")
            .setLongLabel("看漫画")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_comic))
            .setIntent(intent)
            .build()

        shortcuts.add(shortcut)

        intent = Intent(this, VipActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        shortcut = ShortcutInfo.Builder(this, "id4")
            .setShortLabel("VIP解析")
            .setLongLabel("VIP解析")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_video))
            .setIntent(intent)
            .build()

        shortcuts.add(shortcut)

        shortcutManager.dynamicShortcuts = shortcuts
        shortcutManager.updateShortcuts(shortcuts)
    }


    private fun getPosId(): String {
        val posId = intent.getStringExtra("pos_id")
        return if (TextUtils.isEmpty(posId)) StaticData.SPLASH_POS_ID else posId
    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     *
     * @param activity        展示广告的activity
     * @param adContainer     展示广告的大容器
     * @param skipContainer   自定义的跳过按钮：传入该view给SDK后，SDK会自动给它绑定点击跳过事件。SkipView的样式可以由开发者自由定制，其尺寸限制请参考activity_splash.xml或者接入文档中的说明。
     * @param appId           应用ID
     * @param posId           广告位ID
     * @param adListener      广告状态监听器
     * @param fetchDelay      拉取广告的超时时长：取值范围[3000, 5000]，设为0表示使用广点通SDK默认的超时时长。
     */
    private fun fetchSplashAD(
        activity: Activity, adContainer: ViewGroup, skipContainer: View,
        posId: String, adListener: SplashADListener, fetchDelay: Int
    ) {
        splashAD = SplashAD(activity, skipContainer, posId, adListener, fetchDelay)
        splashAD!!.fetchAndShowIn(adContainer)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime(): Boolean {
        val sdf = SimpleDateFormat("HH")
        val hour = sdf.format(Date())
        val k = Integer.parseInt(hour)
        return k in 0..5 || k in 18..23
    }

    /**
     * 倒计时
     */
    private fun delayTime() {
        if (isTiming) return
        isTiming = true
        fr_skip.visibility = View.VISIBLE
        var index = 2
        BaseApplication.mHandler.postDelayed(object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (isPause) {
                    BaseApplication.mHandler.postDelayed(this, 1000)
                    return
                }
                if (index != 0) BaseApplication.mHandler.postDelayed(this, 1000) else {
                    return openMainActivity()
                }
                tv_skip.text = index.toString() + "s跳过"
                index--
            }
        }, 1000)
        fr_skip.setOnClickListener { openMainActivity() }
    }

    override fun onADExposure() {
    }

    override fun onADLoaded(p0: Long) {
    }

    override fun onADDismissed() {
    }

    override fun onADPresent() {
        WjSP.getInstance().setValues(XmlCodes.SPLASHTIME, System.currentTimeMillis())
    }

    override fun onNoAD(p0: AdError?) {
    }

    override fun onADClicked() {
        val link = if (splashAD!!.ext != null) splashAD!!.ext["clickUrl"].toString() else ""
        if (!link.isNull()) {
            startActivity<WebActivity>("url" to link)
        }
    }

    override fun onADTick(p0: Long) {

    }

    override fun onPause() {
        super.onPause()
        isPause = true
        canJump = false
        MobclickAgent.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        isPause = false
        if (canJump) {
            openMainActivity()
        }
        canJump = true
        MobclickAgent.onResume(this)
    }


    override fun onDestroy() {
        img_gg_c.cancelAnimation()
        BaseApplication.mHandler.removeCallbacksAndMessages(null)
        try {
            Glide.with(this).onDestroy()
            System.gc()
        } catch (e: Exception) {
        }
        OhHttpClient.getInit().destroyUrl(Urls.appRuleTips)
        window.decorView.background = null
        super.onDestroy()
    }

    //防止用户返回键退出 APP
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isShowAd && (keyCode == KeyEvent.KEYCODE_BACK
                    || keyCode == KeyEvent.KEYCODE_HOME)
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
