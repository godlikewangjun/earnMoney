package com.wj.commonlib.data.configs

import android.content.Context
import com.abase.okhttp.OhHttpClient
import com.abase.okhttp.db.SQLTools
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.statices.EventCodes
import com.wj.commonlib.statices.XmlConfigs
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.WjSP

/**
 * 配置数据
 * @author Admin
 * @version 1.0
 * @date 2018/6/14
 */
object Configs {
    private var listener: (String)->Unit?={}
    /**
     * 应用平台code
     */
    var CODE: String = ""
        get() = if (field.isEmpty()) WjSP.getInstance().getValues(XmlConfigs.APPCODE, field) else field
    /**
     * 默认渠道
     */
    var CHANNCODE = ""
        get() = WjSP.getInstance().getValues(XmlConfigs.APPCHANNEl, field)

    /**
     * 设置appcode
     */
    fun setAppCode(code: String): Configs {
        CODE=code
        WjSP.getInstance().setValues(XmlConfigs.APPCODE, code)
        return this@Configs
    }

    /**
     * 设置默认的code 优先取xml里面的配置
     */
    fun setChannel(channel: String): Configs {
        WjSP.getInstance().setValues(XmlConfigs.APPCHANNEl, channel)
        return this@Configs
    }

    /**
     * 设置下载文件的保存路径
     */
    fun setDownDir(path: String): Configs {
        OhHttpClient.DOWNDIR=path
        return this@Configs
    }

    /**
     * 设置用户没有权限时或者公共请求错误时的处理
     */
    fun setHttpError(listener:(Int)->Unit): Configs{
        WjEventBus.getInit().subscribe(EventCodes.HTTPERROR,Int::class.java) {
            listener(it as Int)
        }
        return this@Configs
    }

    /**
     * 设置服务端500错误时或者不是200的时候的处理
     */
    fun setServerError(listener:(String)->Unit): Configs{
        this.listener=listener
        WjEventBus.getInit().subscribe(EventCodes.SERVERERROR,String::class.java) {
            listener(it as String)
        }
        return this@Configs
    }
    /**
     * 设置全局超时错误
     */
    fun setServerTimeOut(listener:(String)->Unit): Configs{
        this.listener=listener
        WjEventBus.getInit().subscribe(OhHttpClient.OKHTTP_TIMEOUT,String::class.java) {
            listener(it as String)
        }
        return this@Configs
    }
    /**
     * 如果需要使用数据库就初始化数据库
    */
    fun initSql(context: Context): Configs{
        SQLTools.init(context)
        return this@Configs
    }

}