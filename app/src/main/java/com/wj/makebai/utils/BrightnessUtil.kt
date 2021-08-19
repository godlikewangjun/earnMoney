package com.wj.im.utils

import android.app.Activity
import android.content.ContentResolver
import android.provider.Settings

/**
 * Created by Administrator on 2016/7/27 0027.
 */
object BrightnessUtil {
    /**
     * 判断是否开启了自动亮度调节
     *
     * @return
     */
    fun isAutoBrightness(aContentResolver: ContentResolver): Boolean {
        var automicBrightness = false
        try {
            automicBrightness = Settings.System.getInt(
                aContentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE
            ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        return automicBrightness
    }

    /**
     * 获取屏幕的亮度
     *
     * @param activity
     * @return
     */
    fun getScreenBrightness(activity: Activity): Int {
        var nowBrightnessValue = 0
        val resolver = activity.contentResolver
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(
                resolver, Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return nowBrightnessValue
    }

    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    fun setBrightness(activity: Activity, brightness: Int) {
        // Settings.System.putInt(activity.getContentResolver(),
        // Settings.System.SCREEN_BRIGHTNESS_MODE,
        // Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        val lp = activity.window.attributes
        lp.screenBrightness = java.lang.Float.valueOf(brightness.toFloat()) * (1f / 255f)
        activity.window.attributes = lp
    }

    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    fun setBrightness(activity: Activity, brightness: Float) {
        // Settings.System.putInt(activity.getContentResolver(),
        // Settings.System.SCREEN_BRIGHTNESS_MODE,
        // Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        val lp = activity.window.attributes
        lp.screenBrightness = brightness
        activity.window.attributes = lp
    }

    /**
     * 停止自动亮度调节
     *
     * @param activity
     */
    fun stopAutoBrightness(activity: Activity) {
        Settings.System.putInt(
            activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
    }

    /**
     * 开启亮度自动调节
     *
     * @param activity
     */
    fun startAutoBrightness(activity: Activity) {
        Settings.System.putInt(
            activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )
    }

    /**
     * 保存亮度设置状态
     *
     * @param resolver
     * @param brightness
     */
    fun saveBrightness(resolver: ContentResolver, brightness: Int) {
        val uri = android.provider.Settings.System
            .getUriFor("screen_brightness")
        android.provider.Settings.System.putInt(
            resolver, "screen_brightness",
            brightness
        )
        resolver.notifyChange(uri, null)
    }
}
