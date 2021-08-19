package com.wj.makebai.wxapi

import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.socialize.weixin.view.WXCallbackActivity
import com.wj.makebai.statice.StaticData

class WXPayEntryActivity : WXCallbackActivity(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, StaticData.WX_APPID, true)
        api!!.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {}
    override fun onResp(resp: BaseResp) {
        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Req = resp.errCode
            if (resp.errCode == 0) {
//                ToastUtil.showTip(applicationContext, "支付成功！")
                WxCode = 100
            }
            if (resp.errCode == -2) {
//                ToastUtil.showTip(applicationContext, "支付取消！")
            }
//            if (DialogUtils.playOrder!=null)startActivity<PlayResultActivity>(
//                "orderNum" to GsonUtil.gson2String(DialogUtils.playOrder)
//            )
            finish()
        }
    }

    companion object {
        var WxCode = 50
        var Req = -10
    }
}