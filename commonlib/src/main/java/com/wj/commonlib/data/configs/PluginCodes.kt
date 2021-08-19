package com.wj.commonlib.data.configs

/**
 * 插件一些路径的配置
 * @author dchain
 * @version 1.0
 * @date 2019/8/8
 */
object PluginCodes {
    /**
     * 管理APK的路径
     */
    var managerPath="/data/local/tmp/sample-manager-debug.apk"

    /**
     * 扫一扫的插件路径
     */
    var qrPluginPath= "/data/local/tmp/plugin-debug.zip"
    /**
     * 扫一扫的插件的key
     */
    const val qrPluginKey= "sample-plugin"
    /**
     * 扫一扫的插件的Activity
     */
    const val qrPluginActivity= "com.wj.makebai.plugin.MainActivity"

}