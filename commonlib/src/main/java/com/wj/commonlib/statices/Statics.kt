package com.wj.commonlib.statices

import com.abase.util.GsonUtil
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.UserMode
import com.wj.commonlib.data.mode.dz.DZUserLogin
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull

/**
 * 静态mode
 * @author Admin
 * @version 1.0
 * @date 2018/6/15
 */
object Statics {
    /**
     * 友盟推送设置别名的类型
     */
    val UMPUSHTYPE = "userId"

    /**
     * 用户的资料
     */
    var userMode: UserMode? = null
        @Synchronized set(value) {
            field = value
            WjSP.getInstance()
                .setValues(XmlConfigs.USERMODE, GsonUtil.getGson().toJson(field))
        }
        @Synchronized get() = if (field == null) {
            val user = WjSP.getInstance()
                .getValues(XmlConfigs.USERMODE, "")
            if (user != null) GsonUtil.getGson().fromJson(user, UserMode::class.java) else field
        } else field

    /**oaid 默认存储*/
    var OAID = ""
        set(value) {
            field = value
            WjSP.getInstance()
                .setValues(XmlConfigs.OAID, field)
        }
        get() {
            if (!field.isNull()) return field
            return WjSP.getInstance()
                .getValues(XmlConfigs.OAID, "")
        }

    /**UC TOKEN*/
    var UC_TOKEN = ""
        set(value) {
            field = value
            WjSP.getInstance()
                .setValues(XmlConfigs.UC_TOKEN, field)
        }
        get() {
            return WjSP.getInstance()
                .getValues(XmlConfigs.UC_TOKEN, "")
        }

    /**UC TOKEN*/
    var DZ_LOGIN_INFO :DZUserLogin?=null
        @Synchronized set(value) {
            field = value
            WjSP.getInstance()
                .setValues(XmlConfigs.DZ_INFO, GsonUtil.getGson().toJson(field))
        }
        @Synchronized get() = if (field == null) {
            val user = WjSP.getInstance()
                .getValues(XmlConfigs.DZ_INFO, "")
            if (user != null) GsonUtil.getGson().fromJson(user, DZUserLogin::class.java) else field
        } else field
}