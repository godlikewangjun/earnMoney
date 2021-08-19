package com.wj.commonlib.data.mode

/**
 * 数据模型基类
 * @author Admin
 * @version 1.0
 * @date 2018/6/14
 */
data class BaseMode(
                    var errorCode: Int? = null,
                    var message: String? = null,
                    var data: String? = null)