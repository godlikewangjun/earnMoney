package com.wj.commonlib.statices

import com.wj.ktutils.isNull

/**
 * 配置信息
 * @author wangjun
 * @version 1.0
 * @date 2018/6/2
 */
object Info {
    /**
     * aes 加解密信息
     */
    val iv = ""
    val key = ""
        get() {
            return if (Statics.userMode!=null) Statics.userMode!!.token
            else field
        }
    val money2Points=100
}