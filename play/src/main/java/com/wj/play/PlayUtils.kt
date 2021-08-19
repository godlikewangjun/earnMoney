package com.wj.play

import android.app.Activity
import android.util.Log
import com.alipay.sdk.app.PayTask
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONObject

/**
 * 支付工具
 * @author admin
 * @version 1.0
 * @date 2020/9/27
 */
object PlayUtils {
    /**
     * 支付宝支付
     */
    fun aliPlay(context: Activity, params: String, resultListener: (Map<String, String>) -> Unit){
        val payRunnable = Runnable {
            val alipay = PayTask(context)
            val result =
                alipay.payV2(params.trim(), true)
            resultListener.invoke(result)
        }

        // 必须异步调用
        val payThread = Thread(payRunnable)
        payThread.start()
    }

    /**
     * 微信支付逻辑
     */
    fun wxPay(context: Activity,appId:String,result: String) {
        val api = WXAPIFactory.createWXAPI(context, appId, true)
        api.registerApp(appId)
        try {
            val json = JSONObject(result)
            if (!json.has("retcode")) {
                val req = PayReq()
                req.appId = json.getString("appid")
                req.partnerId = json.getString("partnerid")
                req.prepayId = json.getString("prepayid")
                req.nonceStr = json.getString("noncestr")
                req.timeStamp = json.getString("timestamp")
                req.packageValue = "Sign=WXPay"
                req.sign = json.getString("sign")
                req.extData = "app data" // optional
                api.sendReq(req)
            } else {
                Log.d("PAY_GET", "返回错误" + json.getString("retmsg"))
            }
        } catch (e: Exception) {
            Log.e("PAY_GET", "异常：" + e.message)
        }
    }
}