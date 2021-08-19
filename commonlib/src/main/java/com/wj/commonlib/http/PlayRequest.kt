package com.wj.commonlib.http

import com.wj.commonlib.data.mode.PlayOrderMode
import com.wj.commonlib.data.mode.TaskRecodeMode
import com.wj.commonlib.data.mode.VipCardMode
import com.wj.ktutils.HttpRequests
import com.wj.ktutils.http
import org.json.JSONObject

/**
 * 支付的接口
 * @author admin
 * @version 1.0
 * @date 2020/12/9
 */
object PlayRequest {
    fun aliPay(jsonObject: String,success_:(PlayOrderMode)->Unit){
        Urls.aliPay.post(HttpManager.getParams(jsonObject),success_)
    }

    /**
     * 会员卡的列表
     */
    fun vipCardList(success_:(ArrayList<VipCardMode>)->Unit){
        Urls.vipCardList.post(HttpManager.getParams(null),success_)
    }
}