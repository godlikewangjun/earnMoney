package com.wj.makebai.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import com.abase.okhttp.OhHttpClient
import com.abase.util.AbAppUtil
import com.abase.util.AbFileUtil
import com.abase.util.AbLogUtil
import com.abase.view.weight.QqWebHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.fanjun.keeplive.KeepLive
import com.fanjun.keeplive.config.ForegroundNotification
import com.fanjun.keeplive.config.ForegroundNotificationClickListener
import com.fanjun.keeplive.config.KeepLiveService
import com.qq.e.comm.managers.GDTADManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.beta.upgrade.UpgradeListener
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import com.umeng.socialize.PlatformConfig
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.configs.Configs
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.UpgradeActivity
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.makebai.BuildConfig
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.PermissionCheckActivity
import com.wj.makebai.ui.activity.comm.CustomErrorActivity
import com.wj.makebai.ui.activity.user.LoginActivity
import com.xianwan.sdklibrary.helper.XWAdSdk
import com.zzhoujay.richtext.RichText
import ebz.lsds.qamj.AdManager
import ebz.lsds.qamj.os.OffersManager
import ebz.lsds.qamj.os.PointsManager
import okhttp3.OkHttpClient
import org.android.agoo.xiaomi.MiPushRegistar
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * 应用管理
 * @author Admin
 * @version 1.0
 * @date 2018/5/31
 */
class MyApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
//        installPluginSdk()//插件化

//        OhHttpClient.CONNECTTIMEOUT = 10
//        OhHttpClient.READTIMEOUT = 10
//        OhHttpClient.WRITETIMEOUT = 10
        WjSP.init(this)
        val channel = if (Configs.CHANNCODE.isNull()) AbAppUtil.getMeta(
            this,
            "UMENG_CHANNEL"
        ) else Configs.CHANNCODE

        Configs.setAppCode("n/DnVNHWtE0Jki0vGQ9sbV0gS1+XK7kSDS4qKx45dow=").setChannel(channel)
            .setDownDir(AbFileUtil.getFileDownloadDir(mApplication!!.get())).initSql(this)
            .setHttpError {
                Statics.userMode = null
                val intent = Intent(mApplication!!.get(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                mApplication!!.get()!!.startActivity(intent)
                mHandler.post { mApplication!!.get()!!.showTip("需要重新登录哦~") }
            }.setServerError {
//                val intent = Intent(mApplication!!.get(), WebActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                intent.putExtra("content", it)
//                mApplication!!.get()!!.startActivity(intent)
            }.setServerTimeOut { mHandler.post { showTip("网络不好哦~~") } }
//        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        //不太重要的就延迟加载
        mHandler.postDelayed({
            GDTADManager.getInstance().initWith(this, StaticData.GDT_APPID)
            init()
            //qq浏览器初始化
            QqWebHelper.X5Init(this)
            later()
        }, 1000)
    }
    /**
     * 延时加载不及时使用的功能
     */
    private fun later() {
        //bugly
        Beta.initDelay=1
        Beta.upgradeCheckPeriod = 60 * 1000
        Beta.upgradeListener =
            UpgradeListener { ret, strategy, isManual, isSilence ->
                if (strategy != null && !isSilence) {
                    val i = Intent()
                    i.setClass(applicationContext, UpgradeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                }
            }
        Beta.strToastCheckUpgradeError=""
        Beta.strToastCheckingUpgrade=""
        Beta.strToastYourAreTheLatestVersion=""
//        CrashReport.initCrashReport(this, StaticData.BUGLY, BuildConfig.DEBUG)
        Bugly.init(this, StaticData.BUGLY, BuildConfig.DEBUG)
        //播放器解决音画不同步
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        GSYVideoType.enableMediaCodec()// 打开硬解码
        GSYVideoType.setRenderType(GSYVideoType.SCREEN_MATCH_FULL)
        //有米积分墙
        AdManager.getInstance(applicationContext)
            .init(StaticData.YM_APPID, StaticData.YM_SECRET, true)
        OffersManager.getInstance(this).customUserId = Statics.userMode?.userid.toString()
        //关闭积分到账悬浮框提示功能
        PointsManager.getInstance(this).isEnableEarnPointsToastTips = false
        //关闭积分到账通知栏提示功能
        PointsManager.getInstance(this).isEnableEarnPointsNotification = false


        if (Build.VERSION.SDK_INT < 26) {
            //启动保活
            //定义前台服务的默认样式。即标题、描述和图标
            val foregroundNotification =
                ForegroundNotification(resources.getString(R.string.app_name),
                    if (StaticData.initMode == null) "正在运行" else StaticData.initMode!!.appShareInfo.slogan,
                    R.mipmap.ic_launcher,
                    //定义前台服务的通知点击事件
                    ForegroundNotificationClickListener { _, _ ->
                        val intent = Intent(this, PermissionCheckActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("Notification", true)
                        startActivity(intent)
                    })
            //启动保活服务
            KeepLive.startWork(this, KeepLive.RunMode.ENERGY, foregroundNotification,
                //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
                object : KeepLiveService {
                    /**
                     * 运行中
                     * 由于服务可能会多次自动启动，该方法可能重复调用
                     */
                    override fun onWorking() {
                    }

                    /**
                     * 服务终止
                     * 由于服务可能会被多次终止，该方法可能重复调用，需同onWorking配套使用，如注册和注销broadcast
                     */
                    override fun onStop() {
                    }
                }
            )
        }
    }

    /**
     * 监听各个的activity生命周期
     */
    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {
        }

        override fun onActivityResumed(activity: Activity?) {
        }

        override fun onActivityStarted(activity: Activity?) {
        }

        override fun onActivityDestroyed(activity: Activity?) {
        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity?) {
        }

        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        }

    }

    /**
     * 初始化数据
     */
    fun init() {
        //设置请求超时时间
        OhHttpClient.CONNECTTIMEOUT=10
        OhHttpClient.WRITETIMEOUT=10
        OhHttpClient.READTIMEOUT=10

        GDTADManager.getInstance().initWith(this,StaticData.GDT_APPID)
        RichText.initCacheDir(applicationContext.cacheDir)
        //友盟
        UMConfigure.init(
            this,
            StaticData.UM_APPID,
            Configs.CHANNCODE,
            UMConfigure.DEVICE_TYPE_PHONE,
            StaticData.UM_PUSH_SECRET
        )
        UMConfigure.setLogEnabled(false)
        MobclickAgent.onProfileSignIn(Statics.userMode?.userid.toString())
        // 支持在子进程中统计自定义事件
        UMConfigure.setProcessEvent(true)
        //极光推送
//        JPushInterface.setDebugMode(BuildConfig.DEBUG)
//        JPushInterface.init(this)

        //获取消息推送代理示例
        val mPushAgent: PushAgent = PushAgent.getInstance(this)
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(deviceToken: String) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                AbLogUtil.d("mPushAgent", "注册成功：deviceToken：-------->  $deviceToken")
            }

            override fun onFailure(s: String, s1: String) {
                AbLogUtil.d("mPushAgent", "注册失败：-------->  s:$s -------------- s1:$s1")
            }
        })
        //小米通道
        MiPushRegistar.register(this, "2882303761518293779", "5701829397779")
//        //华为通道，注意华为通道的初始化参数在minifest中配置
//        HuaWeiRegister.register(this)
//        //魅族通道
//        MeizuRegister.register(this, "填写您在魅族后台APP对应的app id", "填写您在魅族后台APP对应的app key")
//        //OPPO通道
//        OppoRegister.register(this, "填写您在OPPO后台APP对应的app key", "填写您在魅族后台APP对应的app secret")
//        //VIVO 通道，注意VIVO通道的初始化参数在minifest中配置
//        VivoRegister.register(this)

        //分享
        PlatformConfig.setWeixin(StaticData.WX_APPID, StaticData.WX_KEY)
        PlatformConfig.setQQZone(StaticData.QQ_APPID, StaticData.QQ_KEy)
        //闲玩
        XWAdSdk.init(this, StaticData.XW_APPID, StaticData.XW_SECRET) //初始化 参数
        XWAdSdk.showLOG(BuildConfig.DEBUG) //是否开启日志

//        if (BuildConfig.DEBUG) CaocConfig.Builder.create()
//            .showErrorDetails(true) //default: true
//            .trackActivities(true) //default: false
//            .errorActivity(CustomErrorActivity::class.java) //default: null (default error activity)
//            .apply()


        try {
            Glide.get(this).registry.replace(
                GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(getSSLOkHttpClient())
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置https 访问的时候对所有证书都进行信任
     *
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    private fun getSSLOkHttpClient(): OkHttpClient {
        val trustManager: X509TrustManager = object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { hostname, session -> true }
            .build()
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        if (Statics.userMode != null)//友盟绑定的userId退出
            MobclickAgent.onProfileSignOff()
        CommTools.exit()
        //关闭数据库
        if(AppDatabase.db.isOpen)AppDatabase.db.close()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
//        if (level == TRIM_MEMORY_MODERATE) {
//            val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
//            i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(i)
//
//            android.os.Process.killProcess(android.os.Process.myPid())
//            exitProcess(0)
//        }

    }
}
