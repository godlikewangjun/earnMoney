package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.google.gson.JsonObject
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.utils.LoadDialog
import com.wj.commonlib.utils.ShareManager
import com.wj.commonlib.utils.setOnClick
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_bind_wechat.*

/**
 * 绑定微信
 * @author admin
 * @version 1.0
 * @date 2020/8/26
 */
class BindWeChatActivity :MakeActivity(),View.OnClickListener{
    override fun bindLayout(): Int {
        return R.layout.activity_bind_wechat
    }

    override fun initData() {
        title_content.text = getString(R.string.bind_wechat)
        wechat.setOnClickListener(this)
    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.wechat -> {
                ShareManager.shareManager!!.login(activity, SHARE_MEDIA.WEIXIN, object :
                    UMAuthListener {
                    override fun onComplete(
                        p0: SHARE_MEDIA?,
                        p1: Int,
                        p2: MutableMap<String, String>?
                    ) {
                        activity.runOnUiThread {
                            val json = JsonObject()
                            json.addProperty("openid", p2!!["openid"])//oYuqo1cpWcT0I0jleHewqPLTmOvM
                            json.addProperty(
                                "iconurl",
                                if (p2["iconurl"] == null || p2["iconurl"].isNull()) "" else p2["iconurl"]
                            )
                            json.addProperty("name", p2["name"])
                            json.addProperty("unionid", p2["unionid"])
                            json.addProperty("sex", if (p2["sex"] == "男") 0 else 1)
//                        json.addProperty("accessToken",p2["accessToken"])
                            HttpManager.bindWeChat(json.toString(),{
                                HttpManager.userDetail ({
                                    showTip("绑定成功")
                                    finish()
                                },null)
                            }){

                            }
                        }
                    }

                    override fun onCancel(p0: SHARE_MEDIA?, p1: Int) {
                        activity.runOnUiThread {
                            LoadDialog.cancle()
                        }
                    }

                    override fun onError(p0: SHARE_MEDIA?, p1: Int, p2: Throwable?) {
                        activity.runOnUiThread {
                            LoadDialog.cancle()
                            activity.showTip(p2!!.message.toString())
                        }
                    }

                    override fun onStart(p0: SHARE_MEDIA?) {
                    }

                }
                )
            }
        }
    }

//    override fun onDestroy() {
//        if (Statics.userMode!!.usertype == 2 && Statics.userMode!!.otherid.isNull())
//            Statics.userMode=null
//        super.onDestroy()
//    }
}