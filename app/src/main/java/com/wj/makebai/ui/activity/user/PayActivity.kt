package com.wj.makebai.ui.activity.user

import android.app.AlertDialog
import android.content.DialogInterface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import com.abase.util.AbViewUtil
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.PlayRequest
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.utils.setOnClick
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.play.PlayUtils
import kotlinx.android.synthetic.main.activity_pay.*

/**
 * 支付页面
 * @author admin
 * @version 1.0
 * @date 2021/1/7
 */
class PayActivity : MakeActivity() {
    private var isPaySuccess = false
    override fun bindLayout(): Int {
        return R.layout.activity_pay
    }

    override fun initData() {
        title_content.text = getString(R.string.pay)

        val payJson = intent.getStringExtra("pay")
        val money = intent.getDoubleExtra("payMoney", 0.0)
        payMoney.text = SpannableStringBuilder("￥$money").apply {
            setSpan(
                AbsoluteSizeSpan(AbViewUtil.dp2px(activity, 15f)),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        pay.text = "支付宝支付￥${money}"
        pay.setOnClick {
            PlayRequest.aliPay(payJson!!) {
                PlayUtils.aliPlay(activity, it.order) {
                    if(it["resultStatus"]=="9000")HttpManager.userDetail({
                        Statics.userMode = it
                        showTip("支付成功")
                        finish()
                    }, null)
                    else{
                        showTip("支付失败")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(activity).setTitle("提示").setMessage("是否放弃支付")
            .setPositiveButton("继续支付",null) .setNegativeButton("放弃支付") { dialog, which -> finish() }.show()
    }
}