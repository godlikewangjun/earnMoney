package com.wj.commonlib.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.bun.miitmdid.core.JLibrary
import java.lang.ref.SoftReference



/**
 * 集成replugin的主程序控制
 * @author Admin
 * @version 1.0
 * @date 2018/5/31
 */
open class BaseApplication : Application() {

    companion object {
        var mApplication: SoftReference<Context>? = null
            get() {
                return if (field != null && field!!.get() == null) {
                    field = SoftReference(BaseApplication().applicationContext!!)
                    field
                } else field
            }
        val mHandler = Handler(Looper.getMainLooper())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        mApplication = SoftReference(this@BaseApplication)
        //oaid替代imei
        JLibrary.InitEntry(this)
        //设置webview多进程报错
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webViewSetPath(this)
        }
    }
    @RequiresApi(api = 28)
    fun webViewSetPath(context:Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(context)
            if (packageName != processName) {//判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }

    private fun getProcessName(context:Context): String? {
        if (context == null) return null
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for ( processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }
}