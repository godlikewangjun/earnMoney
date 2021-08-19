package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.view.View
import com.abase.util.AbDoubleTool
import com.abase.view.weight.RecyclerSpace
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.http.PlayRequest
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.VipCardAdapter
import kotlinx.android.synthetic.main.activity_vip_center.*
import org.json.JSONObject

/**
 * 会员充值
 * @author admin
 * @version 1.0
 * @date 2020/12/7
 */
class VipCenterActivity : MakeActivity() {
    private var adapter: VipCardAdapter? = null
    override fun bindLayout(): Int {
        return R.layout.activity_vip_center
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        title_content.text = "会员充值"



        PlayRequest.vipCardList { list ->
            adapter = VipCardAdapter(list)
            members_input.adapter = adapter
            members_input.addItemDecoration(RecyclerSpace(20))
            members_input.layoutManager = CustomGridManager(activity, 3)
            open_vip.setOnClickListener {
                val mode = list[adapter!!.choose]

                startActivity<PayActivity>("pay" to JSONObject().apply {
                    put("goodName", mode.desc)
                    put("goodId", mode.vipCardId)
                    put("payType", 1)
                    put("goodType", 0)
                }.toString(),"payMoney" to  if(mode.discountNum!=0.0 || mode.discountNum!=1.0 ) AbDoubleTool.mul(mode.payMoney,mode.discountNum) else mode.payMoney)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Statics.userMode != null) {
            glide.load(Statics.userMode!!.usericon).apply(RequestOptions().circleCrop())
                .into(user_icon)
            user_name.text = Statics.userMode!!.username
            user_vip_time.text =if(Statics.userMode!!.vipToDate!=null) "到期时间：${Statics.userMode!!.vipToDate}" else ""
            user_balance.text =  "￥ ${Statics.userMode!!.userbalance}\n余额"
            if(Statics.userMode!!.vipToDate!=null) user_level.text =  "青铜会员" else user_level.visibility=View.GONE
        }
    }
}