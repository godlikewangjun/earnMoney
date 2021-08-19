package com.wj.commonlib.utils

import android.content.Context
import com.abase.util.AbFileUtil
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 未知异常的捕获
 * @author Admin
 * @version 1.0
 * @date 2018/6/20
 */
class RestartHandler(val context:Context):  Thread.UncaughtExceptionHandler{
    //创建服务用于捕获崩溃异常
    override fun uncaughtException(p0: Thread?, e: Throwable) {
        e.printStackTrace()
        //获取日志
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        e.printStackTrace(printWriter)
        var cause: Throwable? = e.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.flush()
        printWriter.close()

        //没有初始化文件夹就初始化
        AbFileUtil.initFileDir(context)
        AbFileUtil.writeAppend("anr", "\n" + SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(System.currentTimeMillis())) + "===" + writer.toString())
//        val intent = baseContext.packageManager
//                .getLaunchIntentForPackage(baseContext.packageName)
//        val restartIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent) // 1秒钟后重启应用
        //退出
        System.exit(0)
        android.os.Process.killProcess(android.os.Process.myPid())

    }
}